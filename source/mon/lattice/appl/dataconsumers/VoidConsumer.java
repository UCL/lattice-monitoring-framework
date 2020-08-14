package mon.lattice.appl.dataconsumers;

import mon.lattice.appl.reporters.EmptyReporter;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractDataConsumer;

/**
 * A BasicConsumer is an object that is used in application 
 * level code.  It has the necessary functionality to act as a consumer
 * and have plugins for each of the data plane, control plane, and
 * info plane.
 */

public class VoidConsumer extends AbstractDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public VoidConsumer() {
	// Use a void reporter
	Reporter reporter =  new EmptyReporter("empty-reporter");
        
	addReporter(reporter);
    }

}