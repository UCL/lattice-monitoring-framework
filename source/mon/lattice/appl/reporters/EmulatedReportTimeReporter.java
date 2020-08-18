package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A EmulatedReportTimeReporter just emulates a report response time.
 */
public class EmulatedReportTimeReporter extends AbstractControllableReporter {
    int emulatedReportTime;
    
    private Logger LOGGER = LoggerFactory.getLogger(EmulatedReportTimeReporter.class);
    
    
    public EmulatedReportTimeReporter(String name, String reportTime) {
        super(name);
        emulatedReportTime = Integer.valueOf(reportTime);
    }
    
    /**
     * In a EmulatedReportTimeReporter, report() just sleeps for reportTime milliseconds.
     */
    @Override
    public void report(Measurement m) {
        try {
            Thread.sleep(emulatedReportTime);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while Sleeping: " + e.getMessage());
        }
    }
}
