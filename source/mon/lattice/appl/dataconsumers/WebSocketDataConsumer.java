package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSON;

/**
 * This receives measurements from a WS Data Plane.
 */
public class WebSocketDataConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a WebSocketDataConsumer
     */
    
    public WebSocketDataConsumer(int dataPort) throws IOException {
	// set up a BasicConsumer (with a built-in PrintReporter)
	consumer = new BasicConsumer();

	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerJSON(dataPort));

	consumer.connect();
    }
    
    
    public WebSocketDataConsumer(String addr, int dataPort) throws IOException {
	// set up a BasicConsumer (with a built-in PrintReporter)
	consumer = new BasicConsumer();

        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerJSON(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        try {
            switch (args.length) {
                case 0:
                    new WebSocketDataConsumer(port);
                    System.err.println("WebSocketDataConsumer listening on ws://*" + ":" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    new WebSocketDataConsumer(port);
                    System.err.println("WebSocketDataConsumer listening on ws://*" + ":" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    new WebSocketDataConsumer(bindAddress, port);
                    System.err.println("WebSocketDataConsumer listening on ws://" + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: WebSocketDataConsumer [port] [endPoint] [bind address]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
