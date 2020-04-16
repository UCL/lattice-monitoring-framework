//author: Alina

package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Timestamp;
import java.io.*;
import mon.lattice.core.ProbeValueWithName;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.form;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
/**
 * A BufferReporter groups and sends the Measurements to a specific function.
 */
public class BufferedRestReporter extends AbstractReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */
    String bufferSize;
    String uri;

    Resty resty = new Resty();
    JSONArray array = new JSONArray();
    private Logger LOGGER = LoggerFactory.getLogger(BufferedRestReporter.class);
    
    public BufferedRestReporter(String reporterName, String bufferSize, String ip, String port, String method) {
        super(reporterName); 
        this.bufferSize = bufferSize;
        this.uri = "http://" + ip + ":" + port + method;
    }

   private void addToBuffer(Measurement m) {
	    if (array.length() <= Integer.parseInt(this.bufferSize))
		array.put(processMeasurement(m));
            
            else {
                    // Send the grouped data and reinitiaze the buffer and counter
                    LOGGER.info("builder result: " + array.toString());
                    
                    try {
                            resty.json(uri, form(array.toString()));
                    } catch (IOException e) {
                            LOGGER.error("Error while sending Measurement: " + e.getMessage());
                        for (StackTraceElement stackTrace : e.getStackTrace()) {
                            LOGGER.error(stackTrace.toString());
                        }
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
                obj.put("value", attribute.getValue().toString());
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
	LOGGER.info("Received measurement: " + m.toString());
    addToBuffer(m);
    }
}