package mon.lattice.appl.reporters;

import java.net.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A reporter that sends the Measurements over UDP
 * encoded in an JSON form.
 */
public class JSONUDPReporter extends AbstractJSONEncoderReporter {
    /*
     * The socket being transmitted to
     */
    protected DatagramSocket socket;

    /*
     * The IP dstAddress being transmitted to
     */
    //InetSocketAddress udpAddr;

    
    /*
     * A packet being transmitted 
     */
    DatagramPacket packet;
    
    /*
     * The IP dstAddress
     */
    InetAddress dstAddress;

    /*
     * The dstPort
     */
    int dstPort;

   
    static int PACKET_SIZE = 65535;

    
    private static Logger LOGGER = LoggerFactory.getLogger(JSONUDPReporter.class);
    

    
    /**
     * Construct a WebSocketReporter for a particular IP dstAddress and dstPort
 parameters are all passed as String to allow dynamic loading from REST API
     */
    
    public JSONUDPReporter(String reporterName, String addr, String port) throws IOException {        
        this(reporterName, InetAddress.getByName(addr), Integer.valueOf(port));
    }
    

    /**
     * Construct a WebSocketReporter for a particular IP dstAddress
     */
    public JSONUDPReporter(String reporterName, InetSocketAddress dstAddr) throws IOException {
        super(reporterName); 
	//wsAddr = dstAddr;

        LOGGER.info("XDRUDPReporter " + dstAddr);
        
	this.dstAddress = dstAddr.getAddress();
	this.dstPort = dstAddr.getPort();

	setUpSocket();
    }

    /**
     * Construct a WebSocketReporter for a particular IP dstAddress
     */
    public JSONUDPReporter(String reporterName, InetAddress addr, int port) throws IOException {
        super(reporterName); 

	//wsAddr = new InetSocketAddress(addr, dstPort);

        //LOGGER.info("WebSocketReporter " + wsAddr);
        
	this.dstAddress = addr;
	this.dstPort = port;

	setUpSocket();
    }


    /**
     * Set up the socket for the given addr/dstPort.
     */
    void setUpSocket() throws IOException {
        socket = new DatagramSocket();

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[1], 1);
	packet.setAddress(dstAddress);
	packet.setPort(dstPort);
    }

    /**
     * Connect to the remote dstAddress now
     */
    public void connect()  throws IOException {
        LOGGER.info("Connecting socket to: " + dstAddress + ":" + dstPort);
        socket.connect(dstAddress, dstPort);
    }

    /**
     * End the connection to the remote dstAddress now
     */
    public void disconnect()  throws IOException {
	// close now
	socket.disconnect();
    }

    
    
    @Override
    public void init() throws IOException {
        LOGGER.info("Connecting");
        connect();
    }
    
    
    @Override
    public void cleanup() throws IOException {
        super.cleanup();
        LOGGER.info("Disconnecting");
        disconnect();
    }

    
    
    @Override
    protected void sendData(byte[] data) throws IOException {
        // set up the packet
	packet.setData(data);
	packet.setLength(data.length);

	// now send it
	socket.send(packet);
    }

}
