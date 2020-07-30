// LifecycleReporter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2020

package mon.lattice.core;

import java.util.EventListener;

/**
 * An interface for Reporters which have some Lifecycle.
 */
public interface LifecycleReporter extends Reporter {
    /**
     * Init a Reporter
     */
    public void init() throws Exception;

    /**
     * Cleanup a Reporter
     */
    public void cleanup() throws Exception;

}
