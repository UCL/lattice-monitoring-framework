package mon.lattice.appl.demo.iot;

import mon.lattice.appl.reporters.JSONWebSocketReporter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;

/**
* A BufferedJSONWebSocketReporter groups the measurements into a JSONArray
* before sending them.
*/

public class BufferedJSONWebSocketReporter extends JSONWebSocketReporter {
    
    private static Logger LOGGER = LoggerFactory.getLogger(BufferedJSONWebSocketReporter.class);
    
    Integer bufferSize;
    
    JSONArray array = new JSONArray();
    

    public BufferedJSONWebSocketReporter(String reporterName, InetSocketAddress dstAddr) throws IOException {
        super(reporterName, dstAddr);
    }

    public BufferedJSONWebSocketReporter(String reporterName, InetAddress addr, int port) throws IOException {
        super(reporterName, addr, port);
    }
    
    
    public BufferedJSONWebSocketReporter(String reporterName, String bufferSize, String ip, String port) throws IOException {
        super(reporterName, InetAddress.getByName(ip), Integer.valueOf(port)); 
        this.bufferSize = Integer.valueOf(bufferSize);
    }    
    
    
    /**
    * In a BufferedJSONWebSocketReporter, report() groups and sends the Measurements
    * as a JSONArray via WebSockets.
    * 
    * @param m the received Measurement
    */
    @Override
    public void report(Measurement m) {
	LOGGER.debug("Received measurement: " + m.toString());
        
        try {
            if (array.length() <= bufferSize)
                array.put(encodeMeasurement(m));
            else {
                 // Send the grouped data and reinitialise the buffer and the counter
                 LOGGER.debug("Array: " + array.toString());
                 sendData(array.toString().getBytes());
                 array = new JSONArray();
                 array.put(encodeMeasurement(m));
                 }
        } catch (IOException e) {
                LOGGER.error("Error while reporting measurement: " + e.getMessage());
        } 
    }
    
}
