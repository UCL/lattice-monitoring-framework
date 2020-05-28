//author: Alina and Francesco

package mon.lattice.appl.reporters;

import java.io.IOException;
import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Timestamp;
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
public class BufferedRestReporterWithCallback extends BufferedRestReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */

    String callbackURI;
    
    private Logger LOGGER = LoggerFactory.getLogger(BufferedRestReporterWithCallback.class);
    
    
    public BufferedRestReporterWithCallback(String reporterName, String bufferSize, String ip, String port, String method, String callbackHost, String callbackPort, String callbackMethod) {
        super(reporterName, bufferSize, ip, port, method);
        this.callbackURI = "http://" + callbackHost + ":" + callbackPort + callbackMethod;
        resty.withHeader("X-Callback-Url", callbackURI);
    }
    
   @Override 
   protected void sendRequest() throws IOException, JSONException {
        String requestID = ID.generate() + ":" + System.currentTimeMillis();
        resty.withHeader("X-Call-Id", requestID);
        Content payload = new Content("application/json", array.toString().getBytes());
        long tStart = System.currentTimeMillis();
        resty.json(uri, payload);
        long tReporting = System.currentTimeMillis() - tStart;
        LOGGER.info("time (msec): " + tReporting);
   }
}