// WSConsumer1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.dataconsumers.BasicConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a WebSocket Data Plane.
 */
public class WSConsumer1 {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a WSConsume1
     */
    public WSConsumer1(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(dataPort);

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerWithNames(address));

	consumer.connect();

    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    new WSConsumer1("localhost", 22997);
	    System.err.println("WSConsume1 listening on localhost/22997");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    new WSConsumer1(addr, port);

	    System.err.println("WSConsume1 listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: WSConsume1 localhost port");
	    System.exit(1);
	}
    }

}
