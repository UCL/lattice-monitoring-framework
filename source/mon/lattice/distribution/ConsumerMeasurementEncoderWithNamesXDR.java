package mon.lattice.distribution;

import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;

/**
 * Convert a ConsumerMeasurementEncoderWithNamesXDR to a XDR representation.
 */
public class ConsumerMeasurementEncoderWithNamesXDR extends MeasurementEncoderWithNamesXDR {
    // This is basically the same as a MeasurementEncoderWithNamesXDR
    // with a few specific differences to get the Probe name
    // and the attribute names

    /**
     * Construct a ConsumerMeasurementEncoderWithNamesXDR for a Measurement.
     */
    public ConsumerMeasurementEncoderWithNamesXDR(Measurement m) {
        this((ConsumerMeasurement)m);
    }
    
    public ConsumerMeasurementEncoderWithNamesXDR(ConsumerMeasurement m) {
        super(m);
    }


    /**
     * Get the probe name
     */
    protected String getProbeName() {
        return ((ConsumerMeasurementWithMetaDataAndProbeName)measurement).getProbeName();
    }

    /**
     * Get an attribute name
     */
    protected String getAttributeName(ProbeValue attr, int field) {
        return ((ProbeValueWithName)attr).getName();
    }

}
