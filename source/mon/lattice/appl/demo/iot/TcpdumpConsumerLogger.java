// TcpdumpConsumerLogger.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives TcpdumpProbe measurements from a WebSocket Data Plane
 * and sends them to a log file
 */
public class TcpdumpConsumerLogger {
    // The  consumer
    TcpdumpConsumer consumer;

    /*
     * Construct a TcpdumpConsumerLogger
     */
    public TcpdumpConsumerLogger(String addr, int dataPort, String logName) {
	// set up a TcpdumpConsumer
	consumer = new TcpdumpConsumer(logName);

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerWithNames(address));
        
	consumer.connect();

    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    new TcpdumpConsumerLogger("localhost", 22997, null);
	    System.err.println("TcpdumpConsumerLogger listening on localhost/22997");

	} else if (args.length == 1) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

	    new TcpdumpConsumerLogger("localhost", port, null);

	    System.err.println("TcpdumpConsumerLogger listening on localhost/" + port);
	} else if (args.length == 2) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

            String filename = args[1];

	    new TcpdumpConsumerLogger("localhost", port, filename);

	    System.err.println("TcpdumpConsumerLogger listening on localhost/" + port);
	} else if (args.length == 3) {
            String hostname = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

            String filename = args[2];

	    new TcpdumpConsumerLogger(hostname, port, filename);

	    System.err.println("TcpdumpConsumerLogger listening on " + hostname + "/" + port);
	} else {
	    System.err.println("usage: TcpdumpConsumerLogger [hostname] [port [log_file_name]]");
	    System.exit(1);
	}
    }

}
