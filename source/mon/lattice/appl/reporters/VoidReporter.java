package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A VoidReporter just emulates a report response time.
 */
public class VoidReporter extends AbstractControllableReporter {
    /**
     * In a VoidReporter, report() just sleeps for reportTime milliseconds.
     */
    
    int emulatedReportTime;
    
    private Logger LOGGER = LoggerFactory.getLogger(VoidReporter.class);
    
    
    public VoidReporter(String name, String reportTime) {
        super(name);
        emulatedReportTime = Integer.valueOf(reportTime);
    }
    
    @Override
    public void report(Measurement m) {
        try {
            Thread.sleep(emulatedReportTime);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while Sleeping: " + e.getMessage());
        }
    }
}
