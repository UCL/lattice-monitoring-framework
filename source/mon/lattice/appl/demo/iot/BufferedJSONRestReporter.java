//author: Alina and Francesco
// edits: Stuart Clayman

package mon.lattice.appl.demo.iot;

import mon.lattice.appl.reporters.JSONRestReporter;
import java.io.IOException;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;


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
    
    
    /**
    * In a BufferedJSONRestReporter, report() groups and sends the Measurements
    * as a JSONArray via REST.
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
