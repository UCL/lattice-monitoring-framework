//author: Alina and Francesco

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ID;
import us.monoid.json.JSONException;
import us.monoid.web.Content;

/**
 * A BufferedJSONRestReporterWithCallback groups and asynchronously sends the 
 * Measurements as a JSONArray to a specific function.
 */
public class BufferedJSONRestReporterWithCallback extends BufferedJSONRestReporter {
    String callbackURI;
    
    private static Logger LOGGER = LoggerFactory.getLogger(BufferedJSONRestReporterWithCallback.class);
    
    
    public BufferedJSONRestReporterWithCallback(String reporterName, String bufferSize, String ip, String port, String method, String callbackHost, String callbackPort, String callbackMethod) {
        super(reporterName, bufferSize, ip, port, method);
        this.callbackURI = "http://" + callbackHost + ":" + callbackPort + callbackMethod;
        resty.withHeader("X-Callback-Url", callbackURI);
    }
   
    
    /**
     * Sends the provided array of bytes to the asynchronous endpoint represented
     * by the @uri member and sets the callback address to @callbackURI in the request header.
     * 
     * @param data
     * @throws IOException
     * @throws JSONException 
     */
    @Override 
    protected void sendData(byte [] data) throws IOException, JSONException {
        String requestID = ID.generate() + ":" + System.currentTimeMillis();
        resty.withHeader("X-Call-Id", requestID);
        Content payload = new Content("application/json", data);
        long tStart = System.currentTimeMillis();
        resty.json(uri, payload);
        long tReporting = System.currentTimeMillis() - tStart;
        LOGGER.info("time (msec): " + tReporting);
   }
}