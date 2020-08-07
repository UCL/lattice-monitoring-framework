package mon.lattice.appl.reporters;

import java.io.IOException;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.ConsumerMeasurementWithMetaDataToJSON;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


/**
 * Encode a single measurement as JSON Object.
 * The measurement includes the MetaData
 * Subclasses can (combine and) send measurements via different transports
 * via implementing the sendData method.
 */
public abstract class AbstractJSONReporter extends AbstractControllableReporter {
    
    public AbstractJSONReporter(String name) {
        super(name);
    }


    /**
     * Implemented by the subclasses according to their specific transport
     * @param data
     * @throws IOException
     * @throws JSONException 
     */
    protected abstract void sendData(byte[] data) throws IOException, JSONException;
    
    
    /**
     * Encode the measurement m as a JSON Object.
     * @param m: the measurement
     * @return the measurement as JSON Object
     */
    protected JSONObject encodeMeasurement(Measurement m) {
        JSONObject obj = new JSONObject();
        try {
            // encode the measurement as JSON with MetaData, ready for transmission
            ConsumerMeasurementWithMetaDataToJSON encoder = new ConsumerMeasurementWithMetaDataToJSON(m);
            // encode into an existing JSONObject
            encoder.encode(obj);
        } catch (TypeException | JSONException e) {
            LoggerFactory.getLogger(getClass()).error("Error while encoding the Measurement: " + e.getMessage());
        }
        return obj;
    }

    @Override
    public void report(Measurement m) {
        LoggerFactory.getLogger(getClass()).debug("Received measurement: " + m.toString());
        try {
            JSONObject measurementsAsJSON = encodeMeasurement(m);
            sendData(measurementsAsJSON.toString().getBytes());
        } catch (IOException | JSONException e) {
            LoggerFactory.getLogger(getClass()).error("Error while sending measurement: " + e.getMessage());
        }
    }
    
}
