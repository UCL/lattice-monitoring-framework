// WSProducer2.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.distribution.ws.WSDataPlaneProducerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a WebSocket Data Plane.
 */
public class WSProducer2 {
    // The DataSource
    DataSource ds;

    /*
     * Construct a WSProducer1.
     */
    public WSProducer2(String addr, int dataPort, String myHostname) {
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
	    System.err.println("WSProducer2 address port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	WSProducer2 producer = new WSProducer2(addr, port, currentHost);

	Probe test = new TestProbe(currentHost + ".vee.2.test", currentHost);

	test.setServiceID(new ID(23456));
	test.setGroupID(new ID(7));
        test.setDataRate(new EveryNSeconds(5));

	producer.turnOnProbe(test);

    }


}

