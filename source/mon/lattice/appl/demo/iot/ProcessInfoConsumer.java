// HostInfoConsumer.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.appl.reporters.PrintReporter;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractDataConsumer;

/**
 * A ProcessInfoConsumer takes HostInfo and logs it.
 */

public class ProcessInfoConsumer extends AbstractDataConsumer implements MeasurementReceiver {
    String filename;
    
    Reporter reporter;

    /**
     * Construct a HostInfoConsumer
     */
    public ProcessInfoConsumer(String filename) {
        this.filename = filename;

        System.err.println("filename = " + filename);

        if (filename == null) {
            // The default way to report a measurement is to print it
            reporter =  new PrintReporter();
        } else {
            reporter = new ProcessInfoReporter(filename);
        }
        
        System.err.println("reporter = " + reporter);

	addReporter(reporter);
    }
}
