// WebSocketReporter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.appl.demo.iot;

import java.net.*;
import java.io.*;
import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import mon.lattice.distribution.ws.SendingWebSocket;
import mon.lattice.distribution.ConsumerMeasurementWithMetaDataToJSON;
import mon.lattice.core.TypeException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONException;


/**
 * A reporter that sends the Measurements over a WebSocket
 * encoded in a JSON form.
 */
public class WebSocketReporter extends AbstractReporter {
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

    

    /**
     * Construct a WebSocketReporter for a particular IP address
     */
    public WebSocketReporter(String reporterName, InetSocketAddress dstAddr) throws IOException {
        super(reporterName); 
	wsAddr = dstAddr;

        System.err.println("WebSocketReporter " + dstAddr);
        
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();

	setUpSocket();
    }

    /**
     * Construct a WebSocketReporter for a particular IP address
     */
    public WebSocketReporter(String reporterName, InetAddress addr, int port) throws IOException {
        super(reporterName); 

	wsAddr = new InetSocketAddress(addr, port);

        System.err.println("WebSocketReporter " + wsAddr);
        
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

            System.out.println("uri = " + uri);
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
    public void disconnect()  throws IOException {
	// close now
	socket.close();
    }


    @Override
    public void report(Measurement m) {
        // Send an incoming Measurement over the WebSocket
        System.err.println("WebSocketReporter: Measurement = " + m.getSequenceNo());


        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        // First convert the Measurement to JSON
        ConsumerMeasurementWithMetaDataToJSON encoder = new ConsumerMeasurementWithMetaDataToJSON(m);

        JSONObject json = new JSONObject();

        try {
            encoder.encode(json);

            // now put the JSONObject into the OutputStream
            String jsonString = json.toString();

            System.err.println("json = " + jsonString);
        
            byteStream.write(jsonString.getBytes());

            // now send it
            socket.send(byteStream.toByteArray());

            
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (TypeException te) {
            te.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        
    }    
}
