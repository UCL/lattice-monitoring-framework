// HostInfoConsumerLogger.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerWithNames;

/**
 * This receives HostInfoProbe measurements from a WebSocket Data Plane
 * and sends them to a log file
 */
public class ProcessInfoConsumerLogger {
    // The  consumer
    ProcessInfoConsumer consumer;

    /*
     * Construct a HostInfoConsumerLogger
     */
    public ProcessInfoConsumerLogger(String addr, int dataPort, String logName) {
	// set up a HostInfoConsumer
	consumer = new ProcessInfoConsumer(logName);

	// set up data plane
	consumer.setDataPlane(new ZMQDataPlaneConsumerWithNames(dataPort));

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
