//author: Alina and Francesco

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ID;
import us.monoid.json.JSONException;
import us.monoid.web.Content;
/**
 * A BufferReporter groups and sends the Measurements to a specific function.
 */
public class BufferedJSONRestReporterWithCallback extends BufferedJSONRestReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */

    String callbackURI;
    
    private Logger LOGGER = LoggerFactory.getLogger(BufferedJSONRestReporterWithCallback.class);
    
    
    public BufferedJSONRestReporterWithCallback(String reporterName, String bufferSize, String ip, String port, String method, String callbackHost, String callbackPort, String callbackMethod) {
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