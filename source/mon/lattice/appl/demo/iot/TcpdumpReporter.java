// TcpdumpReporter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

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
 * An implementation of a Reporter that logs Tcpdump data to a file.
 */
public class TcpdumpReporter extends AbstractControllableReporter implements ReporterMeasurementType {
    // Filename
    String filename;

    // Stream for output
    FileOutputStream outputStream = null;

    HashMap<String, ProbeValue> values = null;

    // An elapsed time
    long elapsed = 0;

    // The Logger
    Logger logger;

    // Stream refs
    int MASK1 = MASK.APP;
    int MASK2 = 1 << 6;
        
    
    /**
     * Constructor with reporter name and name of log file
     */
    public TcpdumpReporter(String reporterName, String filename) {
        super(reporterName);
        this.filename = filename;
    }
    

    /**
     * Constructor with filename of log file
     */
    public TcpdumpReporter(String filename) {
        super("tcpdumpReporter");
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
        ProbeValue hostname = getAttribute("Name");
        ProbeValue ifName = getAttribute("Interface");
        ProbeValue portNo = getAttribute("Port");
        ProbeValue inBytes = getAttribute("inBytes");
        ProbeValue outBytes = getAttribute("outBytes");
        ProbeValue inPackets = getAttribute("inPackets");
        ProbeValue outPackets = getAttribute("outPackets");
        
        String netLine = timestamp.value() + " " + Timestamp.elapsed(elapsed) + " N " + hostname.getValue() + " " + ifName.getValue() + " " + portNo.getValue() + " " + inBytes.getValue() + " " + inPackets.getValue() + " " + outBytes.getValue()  + " " + outPackets.getValue();
            
        logger.logln(MASK1|MASK2, netLine);

        // now add on the measurement delta
        elapsed += m.getDeltaTime().value();
    }

    
    /**
     * Init the Reporter.  Opens the log file.
     */
    @Override
    public void init() throws Exception {
        // allocate a new logger
        logger = Logger.getLogger("tcpdump");

        // add some extra output channels,
        try {
            // MASK.APP = 1 << 4  === MASK1
            outputStream = new FileOutputStream(filename);
            logger.addOutput(new PrintWriter(outputStream), new BitMask(MASK1));

            //  using mask bit 6 === MASK2
            logger.addOutput(new DBOutputter(filename + ".snd"), new BitMask(MASK2));

            
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
            LoggerFactory.getLogger(TcpdumpReporter.class).error("TcpdumpReporter works with Measurements that are WithNames");
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
        list.add("Tcpdump");
        return list;
    }

    
    
}
