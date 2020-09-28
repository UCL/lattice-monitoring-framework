package mon.lattice.appl.dataconsumers;

import mon.lattice.appl.reporters.VoidReporter;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractLifecycleDataConsumer;

/**
 * A VoidConsumer is a simple implementation of AbstractDataConsumer with an VoidReporter.
 */

public class VoidConsumer extends AbstractLifecycleDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public VoidConsumer() {
	// Use a void reporter
	Reporter reporter =  new VoidReporter("void-reporter");
        
	addReporter(reporter);
    }

}