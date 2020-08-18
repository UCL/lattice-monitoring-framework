package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;


/**
 * A VoidReporter has an empty report method.
 */
public class VoidReporter extends AbstractControllableReporter {
    public VoidReporter(String name) {
        super(name);
    }
    
    /**
     * In a VoidReporter, report() is just empty.
     */
    @Override
    public void report(Measurement m) {}
}
