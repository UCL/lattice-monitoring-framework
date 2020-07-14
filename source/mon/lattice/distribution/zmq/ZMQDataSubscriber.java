package mon.lattice.distribution.zmq;

import mon.lattice.core.TypeException;
import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.Receiving;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 *
 * @author uceeftu
 */
public class ZMQDataSubscriber implements Runnable {
    Receiving receiver;
    
    ZContext context;
    ZMQ.Socket subscriberSocket;
    
    String remoteHost = null;
    String remoteURI = null;
    int localPort = 0;
    int remotePort = 0;
    
    ByteArrayInputStream byteStream;
    
    int length;
    
    Thread myThread;

    boolean threadRunning = false;
    
    Exception lastException;
    
    private String threadName="zmq-data-subscriber";
    

    public ZMQDataSubscriber(Receiving receiver, int port) {
        this.receiver = receiver;
        this.localPort = port;
        
        context = new ZContext(1);
        subscriberSocket = context.createSocket(SocketType.SUB);
    }
    
    
    public ZMQDataSubscriber(Receiving receiver, String remoteHost, int remotePort) {
        this(receiver, remotePort);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }
    
    public ZMQDataSubscriber(Receiving receiver, String uri, ZContext ctx) {
        this.receiver = receiver;
        this.remoteURI = uri;
        
        context = ctx;
        subscriberSocket = context.createSocket(SocketType.SUB);
        //subscriberSocket.setRcvHWM(0);
    }
    
    
    public void bind() {
        subscriberSocket.bind("tcp://*:" + localPort);
    }
    
    public void connect() {
        if (remoteURI != null) {
            // sleeping before connecting to the socket
            // not receiving messages otherwise
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            subscriberSocket.connect(remoteURI);
        }
        else {
            LoggerFactory.getLogger(ZMQDataSubscriber.class).debug("Connecting to: " + remoteHost + ":" + remotePort);
            subscriberSocket.connect("tcp://" + remoteHost + ":" + remotePort);
        }
    }
    
    public void listen() {
        myThread = new Thread(this, threadName);
        myThread.start();
    }
    
    public void end() throws InterruptedException {
        threadRunning = false;
    }
    
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

            subscriberSocket.recv(); // header
            
	    // receive from socket
	    byte[] message = subscriberSocket.recv();

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(message, 0, message.length);

	    byteStream = theBytes;
	    //srcAddr = packet.getAddress();
	    length = message.length;
            //srcPort = packet.getPort();


	    return true;
	} catch (ZMQException ze) {
            LoggerFactory.getLogger(ZMQDataSubscriber.class).debug("ZMQException: " + ze.getMessage());
            subscriberSocket.close();
            LoggerFactory.getLogger(ZMQDataSubscriber.class).debug("Socket closed");
            lastException = ze;
           return false;         
          } 
          catch (Exception e) {
	    // something else went wrong
            LoggerFactory.getLogger(ZMQDataSubscriber.class).debug("Exception: " + e.getMessage());
	    lastException = e;
	    return false;
	}
    }

    
    
    @Override
    public void run() {
        threadRunning = true;
        subscriberSocket.subscribe("data".getBytes());
	while (threadRunning) {
	    if (receive()) {
		// now notify the receiver with the replyMessage
		// and the address it came in on
		try {
                    // construct the transmission meta data
                    ZMQTransmissionMetaData metaData = new ZMQTransmissionMetaData(length, subscriberSocket.getRcvHWM(), subscriberSocket.getIdentity());
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
                if (threadRunning) {
                    receiver.error(lastException);
                }
	    }
	}
        subscriberSocket.close();
        context.destroy();
    }
    
}
