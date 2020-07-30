// HostInfoProducer.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
//import mon.lattice.distribution.ws.WSDataPlaneProducerJSON;
import mon.lattice.distribution.ws.WSDataPlaneProducerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.net.UnknownHostException;

/**
 * This monitor sends Host Info data and uses a WebSocket Data Plane.
 */
public class HostInfoProducer {
    // The DataSource
    DataSource ds;

    /*
     * Construct a HostInfoProducer.
     */
    public HostInfoProducer(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up  address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new WSDataPlaneProducerWithNames(address));

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

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 2) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	} else {
	    System.err.println("HostInfoProducer address port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	}

	// we got a hostname
	HostInfoProducer producer = new HostInfoProducer(addr, port, currentHost);

	Probe probe = new HostInfoProbe(currentHost, 5);

	//probe.setServiceID(new ID(12345));
	//probe.setGroupID(new ID(2));

	producer.turnOnProbe(probe);

    }


}
