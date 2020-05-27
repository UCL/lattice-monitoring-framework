//author: Alina

package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Timestamp;
import java.io.*;
import mon.lattice.core.ID;
import mon.lattice.core.ProbeValueWithName;
import us.monoid.web.Resty;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Content;
/**
 * A BufferReporter groups and sends the Measurements to a specific function.
 */
public class BufferedRestReporter extends AbstractReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */
    Integer bufferSize;
    String uri;
    String callbackURI;

    Resty resty = new Resty();
    JSONArray array = new JSONArray();
    private Logger LOGGER = LoggerFactory.getLogger(BufferedRestReporter.class);
    
    public BufferedRestReporter(String reporterName, String bufferSize, String ip, String port, String method, String callbackHost, String callbackPort, String callbackMethod) {
        super(reporterName); 
        this.bufferSize = Integer.valueOf(bufferSize);
        this.uri = "http://" + ip + ":" + port + method;
        this.callbackURI = "http://" + callbackHost + ":" + callbackPort + callbackMethod;
        
        resty.withHeader("X-Callback-Url", callbackURI);
    }

   private void addToBuffer(Measurement m) {
	    if (array.length() <= bufferSize)
		array.put(processMeasurement(m));
            
            else {
                    // Send the grouped data and reinitiaze the buffer and counter
                    LOGGER.debug("builder result: " + array.toString());
                    
                    try {
                            Content payload = new Content("application/json", array.toString().getBytes()); 
                            String requestID = ID.generate() + ":" + System.currentTimeMillis();
                            resty.withHeader("X-Call-Id", requestID);
                            resty.json(uri, payload);
                    } catch (IOException e) {
                            LOGGER.error("Error while sending Measurement: " + e.getMessage());
                    } finally {
                            array = new JSONArray();
                            array.put(processMeasurement(m));
                    }
		}
	}

   private JSONObject processMeasurement(Measurement m)
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
        long tStart = System.currentTimeMillis();
	LOGGER.debug("Received measurement: " + m.toString());
        addToBuffer(m);
        long tReporting = System.currentTimeMillis() - tStart;
        LOGGER.debug("time: " + tReporting);
    }
}