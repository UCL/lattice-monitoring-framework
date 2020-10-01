// ProcessInfo.java
// Author: Francesco Tusa
// Date: October 2020

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
import org.slf4j.LoggerFactory;

/**
 * A probe to get UDP socket info on a Linux system.
 * It uses /proc/net/udp6 to read the underlying data.
 */
public class UDPSocketInfo extends AbstractProbe implements Probe  {
    // A SocketStat object that reads info about the Socket.
    SocketStat socketStat;

    String hostName;
    
    int socketLocalPort;

    /*
     * Construct a ProcessInfo probe
     * given a process id and a hostName.
     * Default data rate is set to every 5 seconds
     */
    public UDPSocketInfo(int port, String hostName) {
	this(port, hostName, 5);  
    }
    
    
    public UDPSocketInfo(int port, String hostName, int dataRate) {
        this.hostName = hostName;
        this.socketLocalPort = port;

        // set probe name
        setName(this.socketLocalPort + ".UDPSocketInfo");
        setDataRate(new EveryNSeconds(dataRate));

        // allocate socketStat
        socketStat = new SocketStat(port);

	// determine actual attributes
        int field = 0;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "hostName", ProbeAttributeType.STRING, "name"));
        field++;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "local_address", ProbeAttributeType.STRING, "n"));
        field++;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "local_port", ProbeAttributeType.INTEGER, "port"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "remote_address", ProbeAttributeType.STRING, "n"));
        field++;
        
        addProbeAttribute(new DefaultProbeAttribute(field, "remote_port", ProbeAttributeType.INTEGER, "port"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "tx_queue", ProbeAttributeType.LONG, "n"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "rx_queue", ProbeAttributeType.LONG, "n"));
        field++;
	    
        addProbeAttribute(new DefaultProbeAttribute(field, "drops", ProbeAttributeType.LONG, "n"));
        field++;
        
    }
    
    /* This is to be used by via the REST API
    *
    */
    public UDPSocketInfo(String hostName, String dataRate, String port) {
        this(Integer.valueOf(port), hostName, Integer.valueOf(dataRate));
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
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();

        int field=0;
        
	// read the data
	if (socketStat.read()) {
	    try {
		// now collect up the results

                
                list.add(new DefaultProbeValue(field, hostName));
                field++;
                
                // add total
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().localAddress));
                field++;
                
                list.add(new DefaultProbeValue(field, socketLocalPort));
                field++;
                                
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().remoteAddress));
                field++;
	    
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().remotePort));
                field++;
	    
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().txQueue));
                field++;
	    
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().rxQueue));
                field++;
	    
                list.add(new DefaultProbeValue(field, socketStat.getSocketStatData().drops));
                field++;
	    	    
                return new ProducerMeasurement(this, list, "UDPSocketInfo");
	    } catch (Exception e) {
                LoggerFactory.getLogger(UDPSocketInfo.class).error("Exception: " + e.getMessage());
		return null;
	    }
	} else {
	    LoggerFactory.getLogger(UDPSocketInfo.class).error("Failed to read from /proc/net/udp6, local port: " + socketLocalPort);
	    return null;
	}
    }

}
