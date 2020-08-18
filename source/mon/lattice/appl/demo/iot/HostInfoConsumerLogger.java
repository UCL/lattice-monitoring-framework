// HostInfoConsumerLogger.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives HostInfoProbe measurements from a WebSocket Data Plane
 * and sends them to a log file
 */
public class HostInfoConsumerLogger {
    // The  consumer
    HostInfoConsumer consumer;

    /*
     * Construct a HostInfoConsumerLogger
     */
    public HostInfoConsumerLogger(String addr, int dataPort, String logName) {
	// set up a HostInfoConsumer
	consumer = new HostInfoConsumer(logName);

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerWithNames(address));

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
	    new HostInfoConsumerLogger("localhost", 22997, null);
	    System.err.println("HostInfoConsumerLogger listening on localhost/22997");

	} else if (args.length == 1) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

	    new HostInfoConsumerLogger("localhost", port, null);

	    System.err.println("HostInfoConsumerLogger listening on localhost/" + port);
	} else if (args.length == 2) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

            String filename = args[1];

	    new HostInfoConsumerLogger("localhost", port, filename);

	    System.err.println("HostInfoConsumerLogger listening on localhost/" + port);
	} else {
	    System.err.println("usage: HostInfoConsumerLogger [port [log_file_name]]");
	    System.exit(1);
	}
    }

}
