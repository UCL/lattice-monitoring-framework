package mon.lattice.appl.dataconsumers;

import mon.lattice.distribution.rest.JSONRestDataPlaneConsumer;
import java.io.IOException;
import java.util.Scanner;
import mon.lattice.core.AbstractDataConsumer;

/**
 * A RestDataConsumer receives measurements from a REST Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing
 */
public class RestDataConsumer {
    // The consumer
    AbstractDataConsumer consumer;

    /*
     * Construct a RestDataConsumer
     */
    
    private RestDataConsumer(boolean printOutput) {
        if (printOutput) {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
        
        else {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
    }
    
    
    public RestDataConsumer(int dataPort, String endPoint, boolean printOutput) throws IOException {
	this(printOutput);

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(dataPort, endPoint));

	consumer.connect();
    }
    
    
    public RestDataConsumer(int dataPort, String endPoint, int threads, boolean printOutput) throws IOException {
	this(printOutput);

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(dataPort, endPoint, threads));

	consumer.connect();
    }
    
    
    public RestDataConsumer(String addr, int dataPort, String endPoint, boolean printOutput) throws IOException {
	this(printOutput);

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort, endPoint));

	consumer.connect();
    }
    
    
    public RestDataConsumer(String addr, int dataPort, String endPoint, int threads, boolean printOutput) throws IOException {
	this(printOutput);

	// set up data plane
	consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort, endPoint, threads));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        boolean printOutput = true;
        int nThreads;
        
        String endPoint ="reporter";
        try {
            switch (args.length) {
                case 0:
                    new RestDataConsumer(port, endPoint, printOutput);
                    System.err.println("RestDataConsumer (printOutput=" + printOutput + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;
                case 3:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    sc = new Scanner(args[2]);
                    printOutput = sc.nextBoolean();
                    new RestDataConsumer(port, endPoint, printOutput);
                    System.err.println("RestDataConsumer (printOutput=" + printOutput + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;    
                case 4:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    sc = new Scanner(args[2]);
                    nThreads = sc.nextInt();
                    sc = new Scanner(args[3]);
                    printOutput = sc.nextBoolean();
                    new RestDataConsumer(port, endPoint, nThreads, printOutput);
                    System.err.println("RestDataConsumer (printOutput=" + printOutput + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
                    System.err.println("Number of threads: " + nThreads);
                    break;
                case 5:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    bindAddress = args[2];
                    sc = new Scanner(args[3]);
                    nThreads = sc.nextInt();
                    sc = new Scanner(args[4]);
                    printOutput = sc.nextBoolean();
                    new RestDataConsumer(bindAddress, port, endPoint, nThreads, printOutput);
                    System.err.println("RestDataConsumer (printOutput=" + printOutput + ") listening on http://" + bindAddress + ":" + port + "/" + endPoint + "/");
                    System.err.println("Number of threads: " + nThreads);
                    break;
                default:
                    System.err.println("usage: RestDataConsumer [port] [endPoint] [bind address] [n_threads] [true | false]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
