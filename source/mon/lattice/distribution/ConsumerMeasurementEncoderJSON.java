// ConsumerMeasurementEncoderJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution;

import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;

/**
 * Convert a ConsumerMeasurement to a JSON representation.
 * The ConsumerMeasurement is expected to be with names
 */
public class ConsumerMeasurementEncoderJSON extends MeasurementEncoderJSON {
    // This is basically the same as a MeasurementEncoderJSON
    // with a few specific differences to get the Probe name
    // and the attribute names

    /**
     * Construct a MeasurementEncoderJSON for a Measurement.
     */
    public ConsumerMeasurementEncoderJSON(Measurement m) {
        this((ConsumerMeasurement)m);
    }
    
    public ConsumerMeasurementEncoderJSON(ConsumerMeasurement m) {
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
