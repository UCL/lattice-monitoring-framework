// WSTransmitter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.Transmitting;
import java.net.*;
import java.io.*;

/**
 * This is a WS transmitter for monitoring messages
 */
public class WSTransmitter {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    /*
     * The socket being transmitted to
     */
    SendingWebSocket socket;

    /*
     * The IP address being transmitted to
     */
    InetSocketAddress wsAddr;

    /*
     * The IP address
     */
    InetAddress address;

    /*
     * The port
     */
    int port;

    /*
     * The URI
     */
    URI uri;

    
    static int PACKET_SIZE = 65535; // was 1500;
    
    /**
     * Construct a transmitter for a particular IP address
     */
    public WSTransmitter(Transmitting transmitting, InetSocketAddress dstAddr) throws IOException {
	wsAddr = dstAddr;

	this.transmitting = transmitting;
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();

	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared Datagrapacket.
     */
    void setUpSocket() throws IOException {
        try {
            // Example new URI( "ws://localhost:8887" )
            this.uri = new URI("ws",  null, address.getHostAddress(), port, null, null, null);

            System.out.println("uri = " + uri);
        //} catch (MalformedURLException mue) {
        } catch (URISyntaxException use) {
            throw new IOException(use.getMessage());
        }
    }

    /**
     * Connect to the remote address now
     */
    public void connect()  throws IOException {
	// create and connect to the remote WS socket
        socket = new SendingWebSocket(uri);
        System.out.println("socket = " + socket);
        socket.connect();
    }

    /**
     * End the connection to the remote address now
     */
    public void end()  throws IOException {
	// close now
	socket.close();
    }

    /**
     * Send a message to WS address,  with a given id.
     */
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
	// now send it
	socket.send(byteStream.toByteArray());
        
        
	//System.err.println("trans: " + id + " = " + byteStream.size());

	// notify the transmitting object
	if (transmitting != null) {
	    transmitting.transmitted(id);
        }

	return byteStream.size();
    }
}
