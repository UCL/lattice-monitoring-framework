// Based on HostInfoProducer.java (from Stuart Clayman)

package mon.lattice.appl.demo.iot;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.net.UnknownHostException;
import mon.lattice.distribution.zmq.ZMQDataPlaneProducerWithNames;

/**
 * This monitor sends ProcessInfo data and uses a ZMQ Data Plane.
 */
public class ProcessInfoProducer {
    // The DataSource
    DataSource ds;

    /*
     * Construct a ProcessInfoProducer.
     */
    public ProcessInfoProducer(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up  address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new ZMQDataPlaneProducerWithNames(address));

	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.deactivateProbe(p);
	ds.removeProbe(p);
    }

    public static void main(String [] args) {
	String addr = "localhost";
	int port = 22997;
        int pid = 1;
        
	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 3) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();
            
            sc = new Scanner(args[2]);
            pid = sc.nextInt();

	} else {
	    System.err.println("ProcessInfoProducer address port pid");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	}

	// we got a hostname
	ProcessInfoProducer producer = new ProcessInfoProducer(addr, port, currentHost);

	Probe probe = new ProcessInfoProbe(currentHost, 5, pid);

	//probe.setServiceID(new ID(12345));
	//probe.setGroupID(new ID(2));

	producer.turnOnProbe(probe);

    }


}
