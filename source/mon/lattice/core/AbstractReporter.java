// AbstractReporter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.core;

/**
 *
 * An abstract implementation of a LifecycleReporter
 */
public abstract class AbstractReporter implements LifecycleReporter {
    public AbstractReporter() {
    }
           
    /**
     * Report the Measurement
     */
    @Override
    public abstract void report(Measurement m);

    
    /* The implementation is left to the subclasses */
    
    /**
     * Init a Reporter
     */
    @Override
    public void init() throws Exception {}

    /**
     * Cleanup a Reporter
     */
    @Override
    public void cleanup() throws Exception {}
    
    
    
}
