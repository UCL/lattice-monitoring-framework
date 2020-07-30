package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A PrintReporter just prints a Measurement.
 */
public final class LoggerReporter extends AbstractControllableReporter {
    /**
     * In a LoggerReporter, report() just logs the Measurement to the log file.
     */
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);
    
    
    public LoggerReporter(String reporterName) {
        super(reporterName); 
    }
    
    
    @Override
    public void report(Measurement m) {
	LOGGER.info(m.toString());
    }
}
