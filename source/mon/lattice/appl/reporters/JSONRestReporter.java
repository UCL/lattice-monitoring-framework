package mon.lattice.appl.reporters;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.web.Resty;
import us.monoid.json.JSONException;
import us.monoid.web.Content;

/**
 * A JSONRestReporter converts a received measurement to JSON and POSTs it
 * to a remote receiver via REST.
 */
public class JSONRestReporter extends AbstractJSONEncoderReporter {

    protected Resty resty = new Resty();
    protected String uri;
    
    private static Logger LOGGER = LoggerFactory.getLogger(JSONRestReporter.class);
    
    
    public JSONRestReporter(String reporterName, String ip, String port, String method) {
        super(reporterName);
        this.uri = "http://" + ip + ":" + port + method;
    }
    
    /**
     * Sends the measurement via REST and waits for the reply. 
     * 
     * @param data is the measurement as an array of bytes
     * @throws IOException
     */
    @Override
    protected void sendData(byte [] data) throws IOException {
        try {
            Content payload = new Content("application/json", data);
            resty.json(uri, payload).object();
        } catch (JSONException je) {
            throw new IOException("Error while sending measurement: " + je.getMessage());
        }
    }
}
