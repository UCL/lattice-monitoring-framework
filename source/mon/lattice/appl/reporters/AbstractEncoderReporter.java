package mon.lattice.appl.reporters;

import java.io.IOException;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import org.slf4j.LoggerFactory;

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
    
    protected long nMeasurements;
    
    public AbstractEncoderReporter(String name) {
        super(name);
        nMeasurements = 0;
    }

    /**
     * Implemented by the subclasses according to their specific transport
     * @param data
     * @throws IOException
     */
    protected abstract void sendData(byte[] data) throws IOException;

    /**
     * Encode the measurement m as a byte array.The specific type of encoding is left to the subclasses
     * @param m: the measurement
     * @return the measurement encoded as array of bytes
     * @throws IOException
     */
    protected abstract byte[] encodeMeasurement(Measurement m) throws IOException;
    
    
    @Override
    public void cleanup() throws IOException {
        LoggerFactory.getLogger(AbstractEncoderReporter.class).info("Reported " + nMeasurements + " measurements");
    }
    
    
    @Override
    public void report(Measurement m) {
        nMeasurements ++;
    }
}
