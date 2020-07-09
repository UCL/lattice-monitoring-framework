package mon.lattice.appl.demo.iot;

import java.io.IOException;
import mon.lattice.appl.dataconsumers.*;
import java.net.InetAddress;
import java.util.Scanner;
import mon.lattice.appl.reporters.PrintReporter;
import mon.lattice.core.Reporter;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class RestDataConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public RestDataConsumer(String addr, int dataPort) throws IOException {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort));

	consumer.connect();
        
        Reporter r = new PrintReporter();
        
        consumer.addReporter(r);

    }

    public static void main(String [] args) {
        String currentHost="localhost";
        int port = 9999;
        try {
            currentHost = InetAddress.getLoopbackAddress().getHostName();
            
            if (args.length == 0) {
                new RestDataConsumer(currentHost, port);
                System.err.println("RestDataConsumer listening on " + currentHost + "/" + port);
            } else if (args.length == 2) {
                String addr = args[0];

                Scanner sc = new Scanner(args[1]);
                port = sc.nextInt();

                new RestDataConsumer(addr, port);

                System.err.println("RestDataConsumer listening on " + addr + "/" + port);
            } else {
                System.err.println("usage: RestDataConsumer localhost port");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
