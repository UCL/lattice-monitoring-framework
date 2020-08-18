package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractDataConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;

/**
 * A WebSocketDataConsumer receives measurements from a WS Data Plane.
 * It decodes XDR measurements and can either print them out or doing nothing
 */
public class WebSocketXDRDataConsumer {
    // The consumer
    AbstractDataConsumer consumer;

    /*
     * Construct a WebSocketDataConsumer
     */
    
    private WebSocketXDRDataConsumer(boolean printOutput) {
        if (printOutput) {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
        
        else {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
    }
    
    
    
    public WebSocketXDRDataConsumer(int dataPort, boolean printOutput) throws IOException {    
        this(printOutput);
        
	// set up data plane
	consumer.setDataPlane(new WSDataPlaneConsumerWithNames(dataPort));

	consumer.connect();
    }
    
    
    public WebSocketXDRDataConsumer(String addr, int dataPort, boolean printOutput) throws IOException {
	this(printOutput);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
	consumer.setDataPlane(new WSDataPlaneConsumerWithNames(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        boolean printOutput = true;
        
        try {
            switch (args.length) {
                case 0:
                    new WebSocketXDRDataConsumer(port, printOutput);
                    System.err.println("WebSocketXDRDataConsumer (printOutput=" + printOutput + ") listening on ws://*" + ":" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    printOutput = sc.nextBoolean();
                    new WebSocketXDRDataConsumer(port, printOutput);
                    System.err.println("WebSocketXDRDataConsumer (printOutput=" + printOutput + ") listening on ws://*" + ":" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    printOutput = sc.nextBoolean();
                    new WebSocketXDRDataConsumer(bindAddress, port, printOutput);
                    System.err.println("WebSocketXDRDataConsumer (printOutput=" + printOutput + ") listening on ws://" + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: WebSocketXDRDataConsumer [port] [bind address] [true | false]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
