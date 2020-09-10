package mon.lattice.appl.reporters;

import java.io.IOException;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.ConsumerMeasurementEncoderWithMetaDataJSON;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


/**
 * Encode a single measurement as JSON Object.
 * The measurement includes the MetaData
 * Subclasses can (combine and) send measurements via different transports
 * via implementing the sendData method.
 */
public abstract class AbstractJSONEncoderReporter extends AbstractEncoderReporter {
    
    public AbstractJSONEncoderReporter(String name) {
        super(name);
    }
    
    
    /**
     * Encode the measurement m as a JSON Object and returns it as an array of bytes.
     * @param m: the measurement
     * @return the measurement the JSON Object as an array of bytes
     */
    @Override
    protected byte[] encodeMeasurement(Measurement m) throws IOException {
        JSONObject obj = new JSONObject();
        try {
            // encode the measurement as JSON with MetaData, ready for transmission
            ConsumerMeasurementEncoderWithMetaDataJSON encoder = new ConsumerMeasurementEncoderWithMetaDataJSON(m);
            // encode into an existing JSONObject
            encoder.encode(obj);
        } catch (TypeException | JSONException e) {
            throw new IOException("Error while encoding measurement: " + e.getMessage());
        }
        return obj.toString().getBytes();
    }

    @Override
    public void report(Measurement m) {
        super.report(m);
        LoggerFactory.getLogger(getClass()).debug("Received measurement: " + m.toString());
        try {
            byte[] measurementAsBytes = encodeMeasurement(m);
            sendData(measurementAsBytes);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error while reporting measurement: " + e.getMessage());
        }
    }
    
}
