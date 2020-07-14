package mon.lattice.appl.demo.iot;

import java.io.IOException;
import mon.lattice.appl.dataconsumers.*;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class RestDataConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    
    public RestDataConsumer(int dataPort, String endPoint) throws IOException {
	// set up a BasicConsumer (with a built-in PrintReporter)
	consumer = new BasicConsumer();

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(dataPort, endPoint));

	consumer.connect();
    }
    
    
    public RestDataConsumer(String addr, int dataPort, String endPoint) throws IOException {
	// set up a BasicConsumer (with a built-in PrintReporter)
	consumer = new BasicConsumer();

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort, endPoint));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        String endPoint ="reporter";
        try {
            switch (args.length) {
                case 0:
                    new RestDataConsumer(port, endPoint);
                    System.err.println("RestDataConsumer listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    new RestDataConsumer(port, endPoint);
                    System.err.println("RestDataConsumer listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    bindAddress = args[2];
                    new RestDataConsumer(bindAddress, port, endPoint);
                    System.err.println("RestDataConsumer listening on http://" + bindAddress + ":" + port + "/" + endPoint + "/");
                    break;
                default:
                    System.err.println("usage: RestDataConsumer [port] [endPoint] [bind address]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
