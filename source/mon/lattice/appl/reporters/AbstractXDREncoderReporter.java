package mon.lattice.appl.reporters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.ConsumerMeasurementEncoderWithMetaDataXDR;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import mon.lattice.xdr.XDRDataOutputStream;

/**
 * Encode a single measurement as XDR Object.
 * The measurement includes the MetaData
 * Subclasses can (combine and) send measurements via different transports
 * via implementing the sendData method.
 */
public abstract class AbstractXDREncoderReporter extends AbstractEncoderReporter {
    
    public AbstractXDREncoderReporter(String name) {
        super(name);
    }
    
    
    /**
     * Encode the measurement m as an XDR Object.
     * @param m: the measurement
     * @return the measurement encoded as array of bytes
     */
    @Override
    protected byte[] encodeMeasurement(Measurement m) {
        ConsumerMeasurementEncoderWithMetaDataXDR encoder = new ConsumerMeasurementEncoderWithMetaDataXDR(m);
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);
        
        try {
            encoder.encode(dataOutput);
        } catch (IOException | TypeException e) {
            LoggerFactory.getLogger(getClass()).error("Error while encoding measurements: " + e.getMessage());
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Exception: " + e.getMessage());
            for (StackTraceElement el: e.getStackTrace()) {
                LoggerFactory.getLogger(getClass()).error(el.toString());
            }
        }
        
        return byteStream.toByteArray();
    }
    

    @Override
    public void report(Measurement m) {
        LoggerFactory.getLogger(getClass()).debug("Received measurement: " + m.toString());
        try {
            byte[] measurementAsBytes = encodeMeasurement(m);
            sendData(measurementAsBytes);
        } catch (IOException | JSONException e) {
            LoggerFactory.getLogger(getClass()).error("Error while sending measurement: " + e.getMessage());
        }
    }
    
}
