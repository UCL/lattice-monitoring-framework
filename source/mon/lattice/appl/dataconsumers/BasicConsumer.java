// BasicConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.appl.dataconsumers;

import mon.lattice.appl.reporters.PrintReporter;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractLifecycleDataConsumer;

/**
 * A BasicConsumer is a simple implementation of AbstractDataConsumer with a PrintReporter.
 */

public class BasicConsumer extends AbstractLifecycleDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public BasicConsumer() {
	// The default way to report a measurement is to print it
	Reporter reporter =  new PrintReporter();
        
	addReporter(reporter);
    }

}