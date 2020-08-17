// JSONWebSocketReporter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020
// edits: Francesco

package mon.lattice.appl.reporters;

import java.net.*;
import java.io.*;
import mon.lattice.distribution.ws.SendingWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A reporter that sends the Measurements over a WebSocket
 * encoded in an XDR form.
 */
public class XDRWebSocketReporter extends AbstractXDREncoderReporter {
    /*
     * The socket being transmitted to
     */
    protected SendingWebSocket socket;

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

    
    private static Logger LOGGER = LoggerFactory.getLogger(XDRWebSocketReporter.class);
    

    
    /**
     * Construct a WebSocketReporter for a particular IP address and port
     * parameters are all passed as String to allow dynamic loading from REST API
     */
    
    public XDRWebSocketReporter(String reporterName, String addr, String port) throws IOException {        
        this(reporterName, InetAddress.getByName(addr), Integer.valueOf(port));
    }
    

    /**
     * Construct a WebSocketReporter for a particular IP address
     */
    public XDRWebSocketReporter(String reporterName, InetSocketAddress dstAddr) throws IOException {
        super(reporterName); 
	wsAddr = dstAddr;

        LOGGER.info("WebSocketReporter " + dstAddr);
        
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();

	setUpSocket();
    }

    /**
     * Construct a WebSocketReporter for a particular IP address
     */
    public XDRWebSocketReporter(String reporterName, InetAddress addr, int port) throws IOException {
        super(reporterName); 

	wsAddr = new InetSocketAddress(addr, port);

        LOGGER.info("WebSocketReporter " + wsAddr);
        
	this.address = addr;
	this.port = port;

	setUpSocket();
    }


    /**
     * Set up the socket for the given addr/port.
     */
    void setUpSocket() throws IOException {
        try {
            // Example new URI( "ws://localhost:8887" )
            this.uri = new URI("ws",  null, address.getHostAddress(), port, null, null, null);

            LOGGER.info("uri = " + uri);
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
        LOGGER.info("socket = " + socket);
        socket.connect();
    }

    /**
     * End the connection to the remote address now
     */
    public void disconnect()  throws IOException {
	// close now
	socket.close();
    }

    
    
    @Override
    public void init() throws IOException {
        LOGGER.info("Connecting");
        connect();
    }
    
    
    @Override
    public void cleanup() throws IOException {
        LOGGER.info("Disconnecting");
        disconnect();
    }

    
    
    @Override
    protected void sendData(byte[] data) throws IOException {
        socket.send(data);
    }

}
