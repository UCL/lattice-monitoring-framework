package mon.lattice.appl.reporters;

import java.io.IOException;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import us.monoid.json.JSONException;

/**
 * An AbstractEncoderReporter that defines the methods to re-encode
 * the received measurements (and returning them as a bytes array) and to
 * send / forward them to another consumer.
 * 
 * The implementation of the specific encoding / transmission mechanisms is 
 * left to the subclasses.
 * 
 * @author uceeftu
 */
public abstract class AbstractEncoderReporter extends AbstractControllableReporter {
    
    public AbstractEncoderReporter(String name) {
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
     * Encode the measurement m as a byte array.
     * The specific type of encoding is left to the subclasses
     * @param m: the measurement
     * @return the measurement encoded as array of bytes
     */
    protected abstract byte[] encodeMeasurement(Measurement m);
    
}
