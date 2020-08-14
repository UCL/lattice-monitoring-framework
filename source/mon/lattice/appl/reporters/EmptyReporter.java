package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;


/**
 * A EmptyReporter has an empty report method.
 */
public class EmptyReporter extends AbstractControllableReporter {
    /**
     * In a EmptyReporter, report() just sleeps for reportTime milliseconds.
     */
    public EmptyReporter(String name) {
        super(name);
    }
    
    @Override
    public void report(Measurement m) {}
}
