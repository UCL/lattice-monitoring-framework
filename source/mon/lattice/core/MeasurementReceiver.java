// MeasurementReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.core;

/**
 * An interface for reporting.
 */
public interface MeasurementReceiver {
    /**
     * Receiver of a measurment, with the measurement.
     */
    public Measurement report(Measurement m);

}
