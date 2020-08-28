// WSConsumerJSONSimple1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: August 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.dataconsumers.BasicConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONSimple;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a WebSocket Data Plane.
 */
public class WSConsumerJSONSimple1 {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a WSConsumerJSONSimple1
     */
    public WSConsumerJSONSimple1(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(address));

        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Shutting down ...");
                    //some cleaning up code...
                    consumer.disconnect();
                }
            });

    	consumer.connect();
        
    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    System.err.println("WSConsumerJSONSimple1 listening on localhost/22997");
	    new WSConsumerJSONSimple1("localhost", 22997);
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    System.err.println("WSConsumerJSONSimple1 listening on " + addr + "/" + port);

	    new WSConsumerJSONSimple1(addr, port);

	} else {
	    System.err.println("usage: WSConsumerJSONSimple1 localhost port");
	    System.exit(1);
	}
    }

}
