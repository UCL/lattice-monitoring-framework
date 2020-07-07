//author: Alina and Francesco

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Timestamp;
import mon.lattice.core.ProbeValueWithName;
import us.monoid.web.Resty;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Content;

/**
 * A BufferReporter groups and sends the Measurements to a specific function.
 */
@Deprecated
public class BufferedRestReporter extends AbstractReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */
    Integer bufferSize;
    String uri;

    Resty resty = new Resty();
    JSONArray array = new JSONArray();
    private Logger LOGGER = LoggerFactory.getLogger(BufferedRestReporter.class);
    
    
    public BufferedRestReporter(String reporterName, String bufferSize, String ip, String port, String method) {
        super(reporterName); 
        this.bufferSize = Integer.valueOf(bufferSize);
        this.uri = "http://" + ip + ":" + port + method;
    }
    
    
   protected void sendRequest() throws IOException, JSONException {
        LOGGER.debug(array.toString());
        Content payload = new Content("application/json", array.toString().getBytes());
        long tStart = System.currentTimeMillis();
        JSONArray result = resty.json(uri, payload).array();
        long tEnd = System.currentTimeMillis();
        long tReporting = tEnd - tStart;
        LOGGER.info("time (msec): " + tEnd/1000 + "," + tReporting);
        LOGGER.debug("result: " + result.toString());
   }
    

   protected void addToBuffer(Measurement m) {
	    if (array.length() <= bufferSize)
		array.put(processMeasurement(m));
            
            else {
                    // Send the grouped data and reinitialise the buffer and the counter
                    LOGGER.debug("builder result: " + array.toString());
                    
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

   protected JSONObject processMeasurement(Measurement m)
        {
        Timestamp t = m.getTimestamp();
        JSONObject obj = new JSONObject();

        for (ProbeValue attribute : m.getValues()) {
            try {
                obj.put("id", m.getServiceID().toString());
                obj.put("probeid", m.getProbeID().toString());
                obj.put("timestamp", t.toString());
                obj.put("type", (((ProbeValueWithName)attribute).getName()));
                obj.put("value", attribute.getValue());
                }
            catch (JSONException e) {
                for (StackTraceElement stackTrace : e.getStackTrace()) {
                    LOGGER.error(stackTrace.toString());
                }
            }
        }    
        return obj;
	}
    
   
    @Override
    public void report(Measurement m) {
        
	LOGGER.debug("Received measurement: " + m.toString());
        addToBuffer(m);
    }
}