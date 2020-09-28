// ProcessInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2020

package mon.lattice.appl.probes.host.linux;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.Rational;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.*;

/**
 * A probe to get cpu info of a Process on a Linux system.
 * It uses /proc/[pid]/stat to read the underyling data.
 */
public class ProcessInfo extends AbstractProbe implements Probe  {
    // A ProcessDev object that reads info about the Process.
    ProcessStat processStat;

    int pid;

    int maWindowSize = 10;

    // Specified in measurements per hour
    // 1200 is once every 3 seconds
    Rational dataRate = new Rational(1200, 1);

    /*
     * Construct a ProcessInfo probe
     * given a process id and a name.
     */
    public ProcessInfo(int pid, String name) {
	setName(name);
        setDataRate(dataRate);

        this.pid = pid;

        // allocate processStat
        // moving average window of 10 by default
        processStat = new ProcessStat(pid, maWindowSize);

	// read data, but calculate nothing
	processStat.read(false);


	// determine actual attributes
        int field = 0;

        addProbeAttribute(new DefaultProbeAttribute(field, "proc-total", ProbeAttributeType.LONG, "n"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "proc-user", ProbeAttributeType.LONG, "n"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "proc-system", ProbeAttributeType.LONG, "n"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "cpu-percent", ProbeAttributeType.FLOAT, "%"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "cpu-average", ProbeAttributeType.FLOAT, "%"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "proc-rss", ProbeAttributeType.LONG, "bytes"));
        field++;
	    
    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
    }

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list 
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(5);

        int field=0;
        
	// read the data
	if (processStat.read(true)) {
	    try {
		// now collect up the results	

                // add total
                list.add(new DefaultProbeValue(field, processStat.getCurrentValue("proc-total")));
                field++;
                                
                list.add(new DefaultProbeValue(field, processStat.getCurrentValue("proc-user")));
                field++;
	    
                list.add(new DefaultProbeValue(field, processStat.getCurrentValue("proc-system")));
                field++;
	    
                list.add(new DefaultProbeValue(field, processStat.getCpuUsageValue("cpu-percent")));
                field++;
	    
                list.add(new DefaultProbeValue(field, processStat.getCpuUsageValue("cpu-average")));
                field++;
	    
                list.add(new DefaultProbeValue(field, processStat.getCurrentValue("proc-rss")));
                field++;
	    	    
		return new ProducerMeasurement(this, list, "ProcessInfo");	
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/" + pid + "/stat");
	    return null;
	}
    }

}
