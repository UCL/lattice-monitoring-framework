package mon.lattice.appl.reporters;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.web.Resty;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONException;
import us.monoid.web.Content;

/**
 * A JSONRestReporter converts a received measurement to JSON and POSTs it
 * to a remote receiver via REST.
 */
public class JSONRestReporterWithLogs extends AbstractJSONEncoderReporter {

    protected Resty resty = new Resty();
    protected String uri;
    
    private static Logger LOGGER = LoggerFactory.getLogger(JSONRestReporterWithLogs.class);
    
    
    public JSONRestReporterWithLogs(String reporterName, String ip, String port, String method) {
        super(reporterName);
        this.uri = "http://" + ip + ":" + port + method;
    }
    
    /**
     * Sends the measurement via REST and waits for the reply.
     * It also logs the result of the request and the time it took
     * to receive the reply.
     * 
     * @param data is the measurement as an array of bytes
     * @throws IOException
     */
    @Override
    protected void sendData(byte [] data) throws IOException {
        Content payload = new Content("application/json", data);
        long tStart = System.nanoTime();
        try {
            JSONObject result = resty.json(uri, payload).object();
            long tReporting = (System.nanoTime() - tStart)/1000;
            LOGGER.info("time (microsec): " + tReporting);
            LOGGER.info("result: " + result.toString());
        } catch (JSONException je) {
            throw new IOException("Error while sending measurement" + je.getMessage());
        }
    }
}
