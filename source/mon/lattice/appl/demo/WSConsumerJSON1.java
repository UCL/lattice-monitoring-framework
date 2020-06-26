// WSConsumerJSON1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.dataconsumers.BasicConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSON;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a WebSocket Data Plane.
 */
public class WSConsumerJSON1 {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a WSConsume1
     */
    public WSConsumerJSON1(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerJSON(address));

	consumer.connect();


        // Can use this for some testing
        /*
        try {
            Thread.sleep(15000);
        } catch (Exception e) {
        }
        
        System.err.println("disconnecting...");
        consumer.disconnect();
        */
    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    System.err.println("WSConsumeJSON1 listening on localhost/22997");
	    new WSConsumerJSON1("localhost", 22997);
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    System.err.println("WSConsumerJSON1 listening on " + addr + "/" + port);

	    new WSConsumerJSON1(addr, port);

	} else {
	    System.err.println("usage: WSConsume1 localhost port");
	    System.exit(1);
	}
    }

}
