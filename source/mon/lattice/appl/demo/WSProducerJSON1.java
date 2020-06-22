// WSProducerJSON1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
import mon.lattice.distribution.ws.WSDataPlaneProducerJSON;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a WebSocket Data Plane.
 */
public class WSProducerJSON1 {
    // The DataSource
    DataSource ds;

    /*
     * Construct a WSProducerJSON1.
     */
    public WSProducerJSON1(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up  address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new WSDataPlaneProducerJSON(address));

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
	    System.err.println("WSProducerJSON1 address port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	WSProducerJSON1 producer = new WSProducerJSON1(addr, port, currentHost);

	Probe random = new RandomProbe(currentHost + ".elapsedTime", "elapsedTime", 15);

	random.setServiceID(new ID(12345));
	random.setGroupID(new ID(2));

	producer.turnOnProbe(random);

    }


}
