// UDPConsumer1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.dataconsumers.BasicConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a WebSocket Data Plane.
 */
public class UDPConsumer1 {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a UDPConsume1
     */
    public UDPConsumer1(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));

	consumer.connect();

    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    new UDPConsumer1("localhost", 22997);
	    System.err.println("UDPConsume1 listening on localhost/22997");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    new UDPConsumer1(addr, port);

	    System.err.println("UDPConsume1 listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: UDPConsume1 localhost port");
	    System.exit(1);
	}
    }

}
