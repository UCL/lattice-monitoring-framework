package mon.lattice.appl.demo.iot;

import java.io.IOException;
import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.ConsumerMeasurementToJSON;
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
public class JSONRestReporter extends AbstractReporter {

    String uri;

    Resty resty = new Resty();
    JSONObject measurementsAsJSON;
    private static Logger LOGGER = LoggerFactory.getLogger(JSONRestReporter.class);
    
    
    public JSONRestReporter(String reporterName, String ip, String port, String method) {
        super(reporterName);
        this.uri = "http://" + ip + ":" + port + method;
    }
    
    
    protected void sendRequest(byte [] data) throws IOException, JSONException {
        Content payload = new Content("application/json", data);
        long tStart = System.currentTimeMillis();
        JSONObject result = resty.json(uri, payload).toObject();
        long tReporting = System.currentTimeMillis() - tStart;
        LOGGER.info("time (msec): " + tReporting);
        LOGGER.info("result: " + result.toString());
    }
    

    protected JSONObject encodeMeasurement(Measurement m) {
        JSONObject obj = new JSONObject();
        try {
            // encode the measurement as JSON, ready for transmission
            ConsumerMeasurementToJSON encoder = new ConsumerMeasurementToJSON(m);
            // encode into an existing JSONObject
            encoder.encode(obj);
        } catch (TypeException | JSONException e) {
            LOGGER.error("Error while encoding the Measurement: " + e.getMessage());
          }
        return obj;
    }
    
    
    /**
    * Report converts the received measurement to JSON and POSTs it via REST.
    */
    @Override
    public void report(Measurement m) {
	LOGGER.debug("Received measurement: " + m.toString());
        try {
            measurementsAsJSON = encodeMeasurement(m);
            sendRequest(measurementsAsJSON.toString().getBytes());
        } catch (IOException | JSONException e) {
            LOGGER.error("Error while sending measurements: " + e.getMessage());
        }
    }
}
