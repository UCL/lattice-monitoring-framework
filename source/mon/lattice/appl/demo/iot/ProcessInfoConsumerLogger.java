// Based on HostInfoConsumerLogger.java (from Stuart Clayman)

package mon.lattice.appl.demo.iot;

import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerWithNames;

/**
 * Receives ProcessInfo measurements from a ZMQ Data Plane
 * and writes them to a log file.
 */
public class ProcessInfoConsumerLogger {
    // The  consumer
    ProcessInfoConsumer consumer;

    /*
     * Construct a ProcessInfoConsumerLogger
     */
    public ProcessInfoConsumerLogger(String addr, int dataPort, String logName) {
	// set up a HostInfoConsumer
	consumer = new ProcessInfoConsumer(logName);

	// set up data plane
	consumer.setDataPlane(new ZMQDataPlaneConsumerWithNames(dataPort));
        
	consumer.connect();

    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    new ProcessInfoConsumerLogger("localhost", 22997, null);
	    System.err.println("ProcessInfoConsumerLogger listening on localhost/22997");

	} else if (args.length == 1) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

	    new ProcessInfoConsumerLogger("localhost", port, null);

	    System.err.println("ProcessInfoConsumerLogger listening on localhost/" + port);
	} else if (args.length == 2) {
	    Scanner sc = new Scanner(args[0]);
	    int port = sc.nextInt();

            String filename = args[1];

	    new ProcessInfoConsumerLogger("localhost", port, filename);

	    System.err.println("ProcessInfoConsumerLogger listening on localhost/" + port);
	} else {
	    System.err.println("usage: ProcessInfoConsumerLogger [port [log_file_name]]");
	    System.exit(1);
	}
    }

}
