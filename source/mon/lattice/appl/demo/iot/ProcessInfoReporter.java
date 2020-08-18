package mon.lattice.appl.demo.iot;

import mon.lattice.appl.demo.iot.*;
import mon.lattice.core.Measurement;
import mon.lattice.core.Timestamp;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.distribution.WithNames;

import cc.clayman.logging.BitMask;
import cc.clayman.logging.Logger;
import cc.clayman.logging.MASK;

import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.ReporterMeasurementType;
import org.slf4j.LoggerFactory;


/**
 *
 * An implementation of a Reporter that logs HostInfo data to a file.
 */
public class ProcessInfoReporter extends AbstractControllableReporter implements ReporterMeasurementType {
    String filename;

    // Stream for output
    FileOutputStream outputStream = null;

    HashMap<String, ProbeValue> values = null;


    // An elapsed time
    long elapsed = 0;

    /**
     * Constructor with reporter name and log file name
     */
    public ProcessInfoReporter(String reporterName, String filename) {
        super(reporterName);
        this.filename = filename;
    }    
    
    
    
    /**
     * Constructor with filename of log file
     */
    public ProcessInfoReporter(String filename) {
        super("processInfoReporter");
        this.filename = filename;
    }
           
    /**
     * Report the Measurement
     */
    @Override
    public void report(Measurement m) {
        measurementToMap(m);

        Timestamp timestamp = m.getTimestamp();

        // host
        ProbeValue hostname = getAttribute("hostName");
        
        ProbeValue pid = getAttribute("pid");
        
        // cpu
        ProbeValue cpu = getAttribute("cpu");
        
        // mem
        ProbeValue mem = getAttribute("mem");


        String metrics = timestamp.value() + " " + Timestamp.elapsed(elapsed) + " C " + hostname.getValue() + " " + pid.getValue() + " " + cpu.getValue() + " "  + mem.getValue();
        
        Logger.getLogger("processinfo").logln(MASK.APP, metrics);

        // now add on the measurement delta
        elapsed += m.getDeltaTime().value();
    }

    
    /**
     * Init the Reporter.  Opens the log file.
     */
    @Override
    public void init() throws Exception {
        // allocate a new logger
        Logger logger = Logger.getLogger("processinfo");

        // add some extra output channels, using mask bit 6
        try {
            outputStream = new FileOutputStream(filename);
            logger.addOutput(new PrintWriter(outputStream), new BitMask(MASK.APP));
        } catch (Exception e) {
            LoggerFactory.getLogger(TcpdumpReporter.class).error(e.getMessage());
        }

    }

    /**
     * Cleanup the Reporter. Closes the log file.
     */
    @Override
    public void cleanup() throws Exception {
        outputStream.close();
    }

    /**
     * convert the Measurement ProbeValues to a Map
     */
    protected void measurementToMap(Measurement m) {
        if (m instanceof WithNames) {
            values = new HashMap<String, ProbeValue>();
            for (ProbeValue pv : m.getValues()) {
                values.put(((ProbeValueWithName)pv).getName(), pv);
            }
        } else {
            LoggerFactory.getLogger(TcpdumpReporter.class).error("ProcessInfoReporter works with Measurements that are WithNames");
        }
    }
    
    /**
     * Get an attribute by name
     */
    protected ProbeValue getAttribute(String name) {
        if (values != null) {
            return values.get(name);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getMeasurementTypes() {
        List<String> list = new ArrayList<>();
        list.add("ProcessInfo");
        return list;
    }

    
    
}
