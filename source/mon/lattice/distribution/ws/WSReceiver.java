// WSReceiver.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.TypeException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * This is a WS receiver for monitoring messages.
 */
public class WSReceiver implements Runnable {
    /*
     * The receiver that interactes messages.
     */
    Receiving receiver = null;

    /*
     * The socket doing the listening
     */
    ReceivingWebSocket socket;

    /*
     * The IP address
     */
    //InetSocketAddress address;

    InetAddress address;

    /*
     * The port
     */
    int port;
    
    
    /*
     * The number of internal decoding Theads 
     */
    int nThreads = 0;
    

    /*
     * My thread.
     */
    Thread myThread;

    boolean threadRunning = false;

    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;

    /*
     * The InetSocketAddress of the last packet received
     */
    InetAddress srcAddr;

    /*
     * The length of the last packet received
     */
    int length;

    /*
     * The source port of the last packet received
     */
    int srcPort;
    
    /*
     * The last exception received.
     */
    Exception lastException;
    
    private String threadName="WSReceiver";


    /**
     * Construct a receiver for a particular IP address
     */
    public WSReceiver(Receiving receiver, InetSocketAddress ipAddr) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.address = ipAddr.getAddress();
	this.port = ipAddr.getPort();
        
	setUpSocket();
    }
    
    /**
     * Construct a receiver for a particular port 
     */
    public WSReceiver(Receiving receiver, int port) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.port = port;
     
        
	setUpSocket();
    }
    
    
     /**
     * Construct a receiver for a particular IP address and number of threads
     */
    public WSReceiver(Receiving receiver, InetSocketAddress ipAddr, int nThreads) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.address = ipAddr.getAddress();
	this.port = ipAddr.getPort();
        this.nThreads = nThreads;
        
	setUpSocket();
    }
    
    /**
     * Construct a receiver for a particular port and number of threads
     */
    public WSReceiver(Receiving receiver, int port, int nThreads) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.port = port;
        this.nThreads = nThreads;
        
	setUpSocket();
    }
    
    
    
    public WSReceiver(Receiving receiver, int port, String name) throws IOException {
	this(receiver, port);
        this.threadName = name;
    }
    
    
    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
        if (this.address == null) {
            if (this.nThreads > 0)
                socket = new ReceivingWebSocket(port, nThreads);
            else
                socket = new ReceivingWebSocket(port);
        } else {
            // InetSocketAddress
            if (this.nThreads > 0)
                socket = new ReceivingWebSocket(new InetSocketAddress(address, port), nThreads);
            else
                socket = new ReceivingWebSocket(new InetSocketAddress(address, port));
        }
        
        socket.setConnectionLostTimeout(100);
        
        // this fixes the exception at startup 
        // if there are pending existing connections on TIME_WAIT
        socket.setReuseAddr(true);
        
        socket.start();
    }

    /**
     * Join the address now
     * and start listening
     */
    public void listen()  throws IOException {
	// already bind to the address
	//socket.bind(address);
        
	// start the thread
	myThread = new Thread(this, this.threadName + "-" + Integer.toString(port));

	myThread.start();
    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void end()  throws IOException {
	// stop the thread
        threadRunning = false;

        try {
            // interrupt the read()
            myThread.interrupt();
            
            // stop the WebSocket
            socket.stop();
            
        } catch (InterruptedException ie) {
            throw new IOException("Socket stop: " + ie.getMessage());
        }
    }

    /**
     * Receive a  replyMessage from the multicast address.
     */
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    ByteBuffer msg = socket.read();

            
	     /*System.out.println("FT: WSReceiver Received " + packet.getLength() +
			   " bytes from "+ packet.getAddress() + 
			   ":" + packet.getPort()); 
            */

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(msg.array());

	    byteStream = theBytes;

	    return true;
	} catch (Exception e) {
	    // something went wrong
	    lastException = e;
	    return false;
	}
    }
    
    public int sendMessage(ByteArrayOutputStream byteStream){
        byte[] replyMessage = byteStream.toByteArray();

        try {
            // now send it
            socket.broadcast(replyMessage);
        } catch (Exception ex) {
            System.out.println("IO error occurred" + ex.getMessage());
            return -1;
        }
        return byteStream.size();
        
    }
    
    /**
     * The Runnable body
     */
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;
        
	while (threadRunning) {
            
	    if (receive()) {
		// construct the transmission meta data
		WSTransmissionMetaData metaData = new WSTransmissionMetaData(length, srcAddr, address, srcPort);

		// now notify the receiver with the replyMessage
		// and the address it came in on
		try {
		    receiver.received(byteStream, metaData);
		} catch (IOException ioe) {
		    receiver.error(ioe);
		} catch (TypeException te) {
		    receiver.error(te);
		} catch (Exception e) {
                      receiver.error(e);
                }
                  
	    } else {
		// the receive() failed
		// we find the exception in lastException
                // we notify the receiver only if the socket was not explicitly closed
                if (threadRunning)
                    receiver.error(lastException);
	    }
	}
    }
	    

}
