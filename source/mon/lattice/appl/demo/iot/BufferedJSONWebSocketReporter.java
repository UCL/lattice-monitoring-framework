package mon.lattice.appl.demo.iot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import mon.lattice.core.Measurement;
import mon.lattice.distribution.ConsumerMeasurementToJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
* A BufferedJSONWebSocketReporter groups the measurements into a JSONArray
* before sending them.
*/

public class BufferedJSONWebSocketReporter extends WebSocketReporter {
    
    private static Logger LOGGER = LoggerFactory.getLogger(BufferedJSONWebSocketReporter.class);
    
    Integer bufferSize;
    
    JSONArray array = new JSONArray();
    

    public BufferedJSONWebSocketReporter(String reporterName, InetSocketAddress dstAddr) throws IOException {
        super(reporterName, dstAddr);
    }

    public BufferedJSONWebSocketReporter(String reporterName, InetAddress addr, int port) throws IOException {
        super(reporterName, addr, port);
    }
    
    
    public BufferedJSONWebSocketReporter(String reporterName, String bufferSize, String ip, String port, String method) throws IOException {
        super(reporterName, InetAddress.getByName(ip), Integer.valueOf(port)); 
        this.bufferSize = Integer.valueOf(bufferSize);
    }

    
    @Override
    public void init() throws IOException {
        LOGGER.info("Connecting");
        super.connect();
    }
    
    
    @Override
    public void cleanup() throws IOException {
        LOGGER.info("Disconnecting");
        super.disconnect();
    }
    
    
    protected void sendRequest() throws IOException, JSONException {
        long tStart = System.currentTimeMillis();
        // now send it
        socket.send(array.toString().getBytes());
        long tReporting = System.currentTimeMillis() - tStart;
        LOGGER.info("time (msec): " + tReporting);
    }
    

    protected void addToBuffer(Measurement m) {
        if (array.length() <= bufferSize)
            array.put(processMeasurement(m));
            
        else {
            // Send the grouped data and reinitialise the buffer and the counter
            LOGGER.info("builder result: " + array.toString());
                    
            try {
                sendRequest();
            } catch (IOException | JSONException e) {
                LOGGER.error("IOException Error while sending Measurement: " + e.getMessage());
            } finally {
                array = new JSONArray();
                array.put(processMeasurement(m));
            }
        }
    }

    protected JSONObject processMeasurement(Measurement m) {
        JSONObject obj = new JSONObject();

        try {
            // encode the measurement as JSON, ready for transmission
            ConsumerMeasurementToJSON encoder = new ConsumerMeasurementToJSON(m);

            // encode into an existing JSONObject
            encoder.encode(obj);
        } catch (Exception e) {
            for (StackTraceElement stackTrace : e.getStackTrace()) {
                LOGGER.error(stackTrace.toString());
            }
        }
    
        return obj;

    }
    
    
    /**
    * In a BufferReporter, report() groups and sends the Measurement to a specific function.
    */
    @Override
    public void report(Measurement m) {
	LOGGER.debug("Received measurement: " + m.toString());
        addToBuffer(m);
    }
    
    
    
}
