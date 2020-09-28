package mon.lattice.distribution;

import java.io.DataOutput;
import java.io.IOException;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;

/**
 * Convert a ConsumerMeasurement that has some Message Meta Data and names to a XDR representation.
 * It extends ConsumerMeasurementEncoderWithMetaDataXDR but also encodes the names by using
 * a ConsumerMeasurementEncoderWithNamesXDR.
 */
public class ConsumerMeasurementEncoderWithMetaDataWithNamesXDR extends ConsumerMeasurementEncoderWithMetaDataXDR {

    public ConsumerMeasurementEncoderWithMetaDataWithNamesXDR(ConsumerMeasurement m) {
        super(m);
    }

    public ConsumerMeasurementEncoderWithMetaDataWithNamesXDR(Measurement m) {
        super(m);
    }

    @Override
    protected void encodeMeasurement(ConsumerMeasurementWithMetaData cm, DataOutput out) throws IOException, TypeException {
        // encode the measurement as XDR, ready for transmission
        ConsumerMeasurementEncoderWithNamesXDR encoder = new ConsumerMeasurementEncoderWithNamesXDR(cm);

        // encode into an existing XDR
        encoder.encode(out);
    }
    
    
    
}
