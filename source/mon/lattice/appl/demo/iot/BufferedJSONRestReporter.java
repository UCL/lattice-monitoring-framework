//author: Alina and Francesco
// edits: Stuart Clayman

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;


/**
 * A BufferReporter groups Measurements into a JSONArray of a given size
 * before sending them to a specific function via REST.
 */
public class BufferedJSONRestReporter extends JSONRestReporter {

    Integer bufferSize;
    JSONArray array = new JSONArray();
    private static Logger LOGGER = LoggerFactory.getLogger(BufferedJSONRestReporter.class);
    
    
    public BufferedJSONRestReporter(String reporterName, String bufferSize, String ip, String port, String method) {
        super(reporterName, ip, port, method); 
        this.bufferSize = Integer.valueOf(bufferSize);
    }
    

    protected void processMeasurement(Measurement m) {
        if (array.length() <= bufferSize)
            array.put(encodeMeasurement(m));
        else {
            // Send the grouped data and reinitialise the buffer and the counter
            LOGGER.debug("builder result: " + array.toString());
                    
            try {
                sendRequest(array.toString().getBytes());
            } catch (IOException | JSONException e) {
                LOGGER.error("IOException Error while sending Measurement: " + e.getMessage());
            } finally {
                array = new JSONArray();
                array.put(encodeMeasurement(m));
            }
        }
    }
    
    
    /**
    * In a BufferReporter, report() groups and sends the Measurement to a specific function.
    */
    @Override
    public void report(Measurement m) {
        
	LOGGER.debug("Received measurement: " + m.toString());
        processMeasurement(m);
    }
}
