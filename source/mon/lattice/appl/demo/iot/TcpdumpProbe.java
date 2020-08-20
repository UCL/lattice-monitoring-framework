// TcpdumpProbe.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import java.util.ArrayList;

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

import mon.lattice.appl.demo.iot.process.TcpdumpWrapper;
import org.slf4j.LoggerFactory;

/**
 * A probe that gets info from tcpdump about traffic coming in and out of a port
 */
public class TcpdumpProbe extends AbstractProbe implements Probe {

    TcpdumpWrapper.Tcpdump tcpdump;

    TcpdumpWrapper tcpdumpListener;
    
    String hostname;

    
    /**
     * Construct a TcpdumpProbe.
     * Used by the REST API
     */
    public TcpdumpProbe(String hostname, String ifName, String port, String datarate) {
        this(hostname, ifName, Integer.valueOf(port), Integer.valueOf(datarate));
    }
    
    
    /**
     * Construct a TcpdumpProbe
     * Pass in hostname, interface name (e.g. eth0), port number.
     */
    public TcpdumpProbe(String hostname, String ifName, int port, int datarate) {
        // set hostname
        this.hostname = hostname;


        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("mac")) {
            // Mac OS
            startTcpdump(ifName, port);
            
        } else if (osName.startsWith("linux")) {
            // Linux
            startTcpdump(ifName, port);
            
        } else {
            throw new Error("TcpdumpProbe: not implemented for " + osName + " yet!");
        }


        // set probe name
        setName(hostname+".tcpdump");
        // set data rate
        setDataRate(new EveryNSeconds(datarate));


        // setup the probe attributes
        // The hostname
        addProbeAttribute(new DefaultProbeAttribute(0, "Name", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "Interface", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(2, "Port", ProbeAttributeType.INTEGER, "name"));
        // Traffic in (bytes)
        addProbeAttribute(new DefaultProbeAttribute(3, "inBytes", ProbeAttributeType.LONG, "bytes"));
        // Traffic out (bytes)
        addProbeAttribute(new DefaultProbeAttribute(4, "outBytes", ProbeAttributeType.LONG, "bytes"));
        // Traffic in (packets)
        addProbeAttribute(new DefaultProbeAttribute(5, "inPackets", ProbeAttributeType.LONG, "packets"));
        // Traffic out (packets)
        addProbeAttribute(new DefaultProbeAttribute(6, "outPackets", ProbeAttributeType.LONG, "packets"));
        

    }


    /**
     * Start a TcpdumpWrapper with tcpdump down the end of a pipe.
     */
    protected void startTcpdump(String ifName, int port) {
        try {
            tcpdump = new TcpdumpWrapper.Tcpdump(ifName, port);

            tcpdumpListener = new TcpdumpWrapper(tcpdump);
        } catch (IOException ioe) {
            LoggerFactory.getLogger(TcpdumpProbe.class).error("Error while starting tcpdump: " + ioe.getMessage());
        }
    }

    /**
     * Collect a measurement.
     */
    @Override
    public ProbeMeasurement collect() {
        try {
            // create a list for Probe Values
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();

            // add the name of the controller
            list.add(new DefaultProbeValue(0, hostname));
            list.add(new DefaultProbeValue(1, tcpdump.getInterface()));
            list.add(new DefaultProbeValue(2, tcpdump.getPort()));


            // get current traffic on that port for the interface
            long[] traffic = tcpdumpListener.getTraffic();

            long inBytes = traffic[0];
            long outBytes = traffic[1];
            long inPackets = traffic[2];
            long outPackets = traffic[3];

            list.add(new DefaultProbeValue(3, inBytes));
            list.add(new DefaultProbeValue(4, outBytes));
            list.add(new DefaultProbeValue(5, inPackets));
            list.add(new DefaultProbeValue(6, outPackets));

            // Create the Measurement
            ProducerMeasurement m = new ProducerMeasurement(this, list, "Tcpdump");

            //System.err.println("m = " + m);

            return m;
        } catch (TypeException te) {
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            // a failure somewhere
            return null;
        }
    }

    /**
     * The code to run at the end of the thread body.
     * Used to tidy things up.
     */
    public void endThreadBody() {
        System.err.println("TcpdumpProbe: endThreadBody");
        tcpdumpListener.stop();
    }
    

}
