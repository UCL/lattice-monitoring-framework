package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;

/**
 * A UDPXDRDataConsumerWithNames receives measurements from a UDP Data Plane.
 * It decodes XDR measurements (with names) and can either print them out or doing nothing
 */
public class UDPXDRDataConsumerWithNames {
    // The consumer
    AbstractLifecycleDataConsumer consumer;

    /*
     * Construct a UDPXDRDataConsumerWithNames
     */
    
    private UDPXDRDataConsumerWithNames(boolean printOutput) {
        if (printOutput) {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
        
        else {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
    }
    
    
    
    public UDPXDRDataConsumerWithNames(int dataPort, boolean printOutput) throws IOException {    
        this(printOutput);
        
	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(dataPort));

	consumer.connect();
    }
    
    
    public UDPXDRDataConsumerWithNames(String addr, int dataPort, boolean printOutput) throws IOException {
	this(printOutput);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        boolean printOutput = true;
        
        try {
            switch (args.length) {
                case 0:
                    new UDPXDRDataConsumerWithNames(port, printOutput);
                    System.err.println("UDPXDRDataConsumerWithNames (printOutput=" + printOutput + ") listening on *:" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    printOutput = sc.nextBoolean();
                    new UDPXDRDataConsumerWithNames(port, printOutput);
                    System.err.println("UDPXDRDataConsumerWithNames (printOutput=" + printOutput + ") listening on *:" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    printOutput = sc.nextBoolean();
                    new UDPXDRDataConsumerWithNames(bindAddress, port, printOutput);
                    System.err.println("UDPXDRDataConsumerWithNames (printOutput=" + printOutput + ") listening on " + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: UDPXDRDataConsumerWithNames [port] [bind address] [true | false]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
