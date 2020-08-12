// HostInfoProbe.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import java.util.ArrayList;
import java.io.IOException;

import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;

import mon.lattice.appl.demo.iot.process.PipeProcess;
import org.slf4j.LoggerFactory;

/**
 * A probe that gets info on a host
 */
public class ProcessInfoProbe extends AbstractProbe implements Probe {
    String hostname;
    
    int processId;

    
    
    /**
     * Construct a ProcessInfoProbe.
     * Used by the REST API
     */
    public ProcessInfoProbe(String hostname, String datarate, String processId) {
        this(hostname, Integer.valueOf(datarate), Integer.valueOf(processId));
    }
    
    
    
    /**
     * Construct a ProcessInfoProbe
     */
    public ProcessInfoProbe(String hostname, int datarate, int processId) {
        // set hostname
        this.hostname = hostname;
        this.processId = processId;

        // set probe name
        setName(this.processId + ".processInfo");
        // set data rate
        setDataRate(new EveryNSeconds(datarate));


        // setup the probe attributes
        // The hostname
        addProbeAttribute(new DefaultProbeAttribute(0, "hostName", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "pid", ProbeAttributeType.INTEGER, "pid"));
        addProbeAttribute(new DefaultProbeAttribute(2, "cpu", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(3, "mem", ProbeAttributeType.FLOAT, "percent"));
    }

    /**
     * Collect a measurement.
     */
    @Override
    public ProbeMeasurement collect() {
        try {
            
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();


            // add the name of the controller
            list.add(new DefaultProbeValue(0, hostname));
            list.add(new DefaultProbeValue(1, processId));


            // get some data from "ps"
            PipeProcess ps = startPSProcess("/usr/bin/env ps -p " + processId + " -o %cpu,%mem");

            String psData = getDataFromPipeProcess(ps);

            processPSData(psData, list);
            
            // Create the Measurement
            ProducerMeasurement m = new ProducerMeasurement(this, list, "ProcessInfo");
            
            return m;

        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Start 'ps' down the end of a pipe.
     */
    protected PipeProcess startPSProcess(String proc) throws IOException {
        // create a subrocess
        String [] processArgs = proc.split(" ");
        ProcessBuilder child = new ProcessBuilder(processArgs);
        Process process = child.start();

        // get a wrapper on the process
        return new PipeProcess(process);
    }


    /**
     * Get Data from PipeProcess
     */
    protected String getDataFromPipeProcess(PipeProcess pipe) throws IOException {
        Process process = pipe.getProcess();

        // wait for the process to actually end
        try {
            process.waitFor();
        } catch (InterruptedException ie) {
            LoggerFactory.getLogger(ProcessInfoProbe.class).error("PipeProcess: process wait for error: " + ie);
        }

        pipe.stop();

        // and collect the output
        String result =  pipe.getData();

        if (result == null) {
            return null;
        } else if (result.length() == 0) {
            throw new IOException("PipeProcess: failed to process data");
        } else {
            //System.err.println("PipeProcess: collected " + result.length());

            return result;
        }
    }


    /*
    * %CPU %MEM
    * 4.2  0.1
    *
    */
    protected void processPSData(String raw, ArrayList<ProbeValue> list) {
        try { 

            // split output lines lines
            String[] parts = raw.split("\n");

            // remove spaces at the start / end and splits on spaces
            String[] stats = parts[1].trim().split("\\s+");
            
            Float cpu = Float.valueOf(stats[0]);
            Float mem = Float.valueOf(stats[1]);
            
            list.add(new DefaultProbeValue(2, cpu));
            list.add(new DefaultProbeValue(3, mem));

        } catch (TypeException te) {
            return;
        }
    }
}
