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
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.*;
import mon.lattice.core.datarate.EveryNSeconds;

/**
 * A probe to get cpu info of a Process on a Linux system.
 * It uses /proc/[pid]/stat to read the underlying data.
 */
public class ProcessInfo extends AbstractProbe implements Probe  {
    // A ProcessDev object that reads info about the Process.
    ProcessStat processStat;

    String hostName;
    
    int pid;

    int maWindowSize = 10;

    /*
     * Construct a ProcessInfo probe
     * given a process id and a hostName.
     * Default data rate is set to every 5 seconds
     */
    public ProcessInfo(int pid, String hostName) {
	this(pid, hostName, 5);  
    }
    
    
    public ProcessInfo(int pid, String hostName, int dataRate) {
        this.hostName = hostName;
        this.pid = pid;

        // set probe name
        setName(this.pid + ".processInfo");
        setDataRate(new EveryNSeconds(dataRate));

        // allocate processStat
        // moving average window of 10 by default
        processStat = new ProcessStat(pid, maWindowSize);

	// read data, but calculate nothing
	processStat.read(false);


	// determine actual attributes
        int field = 0;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "hostName", ProbeAttributeType.STRING, "name"));
        field++;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "pid", ProbeAttributeType.INTEGER, "pid"));
        field++;
        
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
    
    /* This is to be used by via the REST API
    *
    */
    public ProcessInfo(String hostName, String dataRate, String pid) {
        this(Integer.valueOf(pid), hostName, Integer.valueOf(dataRate));
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
                
                list.add(new DefaultProbeValue(field, hostName));
                field++;
                
                list.add(new DefaultProbeValue(field, pid));
                field++;
                
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
