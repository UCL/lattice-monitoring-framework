package mon.lattice.appl.reporters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.ConsumerMeasurementEncoderWithMetaDataWithNamesXDR;
import org.slf4j.LoggerFactory;
import mon.lattice.xdr.XDRDataOutputStream;

/**
 * Encode a single measurement as XDR Object.
 * The measurement includes the MetaData
 * Subclasses can (combine and) send measurements via different transports
 * via implementing the sendData method.
 */
public abstract class AbstractXDREncoderReporterWithNames extends AbstractEncoderReporter {
    
    public AbstractXDREncoderReporterWithNames(String name) {
        super(name);
    }
    
    
    /**
     * Encode the measurement m as an XDR Object.
     * @param m: the measurement
     * @return the measurement encoded as array of bytes
     */
    @Override
    protected byte[] encodeMeasurement(Measurement m) throws IOException {
        ConsumerMeasurementEncoderWithMetaDataWithNamesXDR encoder = new ConsumerMeasurementEncoderWithMetaDataWithNamesXDR(m);
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);
        
        try {
            encoder.encode(dataOutput);
        } catch (IOException | TypeException e) {
            throw new IOException("Error while encoding measurement: " + e.getMessage());
        }
        
        return byteStream.toByteArray();
    }
    

    @Override
    public void report(Measurement m) {
        LoggerFactory.getLogger(getClass()).debug("Received measurement: " + m.toString());
        try {
            byte[] measurementAsBytes = encodeMeasurement(m);
            sendData(measurementAsBytes);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error while reporting measurement: " + e.getMessage());
        }
    }
    
}
