package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerJSON;

/**
 * A UDPJSONDataConsumer receives measurements from a UDP Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing
 */
public class UDPJSONDataConsumer {
    // The consumer
    AbstractLifecycleDataConsumer consumer;

    /*
     * Construct a UDPJSONDataConsumer
     */
    
    private UDPJSONDataConsumer(boolean printOutput) {
        if (printOutput) {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
        
        else {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
    }
    
    
    
    public UDPJSONDataConsumer(int dataPort, boolean printOutput) throws IOException {    
        this(printOutput);
        
	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerJSON(dataPort));

	consumer.connect();
    }
    
    
    public UDPJSONDataConsumer(String addr, int dataPort, boolean printOutput) throws IOException {
	this(printOutput);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
	consumer.setDataPlane(new UDPDataPlaneConsumerJSON(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        boolean printOutput = true;
        
        try {
            switch (args.length) {
                case 0:
                    new UDPJSONDataConsumer(port, printOutput);
                    System.err.println("UDPJSONDataConsumer (printOutput=" + printOutput + ") listening on *:" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    printOutput = sc.nextBoolean();
                    new UDPJSONDataConsumer(port, printOutput);
                    System.err.println("UDPJSONDataConsumer (printOutput=" + printOutput + ") listening on *:" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    printOutput = sc.nextBoolean();
                    new UDPJSONDataConsumer(bindAddress, port, printOutput);
                    System.err.println("UDPJSONDataConsumer (printOutput=" + printOutput + ") listening on " + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: UDPJSONDataConsumer [port] [bind address] [true | false]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
