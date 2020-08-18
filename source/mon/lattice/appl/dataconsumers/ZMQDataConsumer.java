package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.util.Scanner;
import mon.lattice.core.AbstractDataConsumer;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerWithNames;

/**
 * A ZMQDataConsumer receives measurements from a ZMQ Data Plane.
 * It decodes XDR measurements and can either print them out or doing nothing
 */
public class ZMQDataConsumer {
    // The consumer
    AbstractDataConsumer consumer;
    
    
    public ZMQDataConsumer(int dataPort, boolean printOutput) throws IOException {    
        if (printOutput) {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
        
        else {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
        
	// set up data plane
	consumer.setDataPlane(new ZMQDataPlaneConsumerWithNames(dataPort));

	consumer.connect();
    }
    

    public static void main(String [] args) {
        int port = 9999;
        boolean printOutput = true;
        
        try {
            switch (args.length) {
                case 0:
                    new ZMQDataConsumer(port, printOutput);
                    System.err.println("ZMQDataConsumer (printOutput=" + printOutput + ") listening on tcp://*" + ":" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    printOutput = sc.nextBoolean();
                    new ZMQDataConsumer(port, printOutput);
                    System.err.println("ZMQDataConsumer (printOutput=" + printOutput + ") listening on tcp://*" + ":" + port);
                    break;
                default:
                    System.err.println("usage: ZMQDataConsumer [port] [true | false]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
