// PrintReporter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;


/**
 * A PrintReporter just prints a Measurement.
 */
public class PrintReporter extends AbstractControllableReporter {
    /**
     * In a PrintReporter, report() just prints the Measurement.
     */
    
    public PrintReporter() {
        super("print-reporter");
    }
    
    @Override
    public void report(Measurement m) {
	System.out.println(m);
    }
}
