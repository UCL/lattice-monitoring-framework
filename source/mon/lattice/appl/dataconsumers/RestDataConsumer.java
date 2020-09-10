package mon.lattice.appl.dataconsumers;

import mon.lattice.distribution.rest.JSONRestDataPlaneConsumer;
import java.io.IOException;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.rest.JSONRestDataPlaneConsumerProfiled;

/**
 * A RestDataConsumer receives measurements from a REST Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing
 */
public class RestDataConsumer {
    // The consumer
    AbstractLifecycleDataConsumer consumer;
    
    
    byte printOutput;
    byte doProfiling;
    
    
    private void parseConf(byte conf) {
        printOutput = (byte) (conf &  0x01);
        doProfiling = (byte) ((conf & 0x02) >>> 1);
    }

    /*
     * Construct a RestDataConsumer
     */
    
    private RestDataConsumer(byte conf) {
        parseConf(conf);
        
        if (printOutput == 0) {
            // set up a VoidConsumer (no print)
            consumer = new VoidConsumer();
        }
        
        else {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
    }
    
    
    public RestDataConsumer(int dataPort, String endPoint, byte conf) throws IOException {
	this(conf);

	// set up data plane
        
        if (doProfiling == 0)
            consumer.setDataPlane(new JSONRestDataPlaneConsumer(dataPort, endPoint));
        else
            consumer.setDataPlane(new JSONRestDataPlaneConsumerProfiled(dataPort, endPoint));

	consumer.connect();
    }
    
    
    public RestDataConsumer(int dataPort, String endPoint, int threads, byte conf) throws IOException {
	this(conf);

	// set up data plane
        
        if (doProfiling == 0) 
            consumer.setDataPlane(new JSONRestDataPlaneConsumer(dataPort, endPoint, threads));
        else
            consumer.setDataPlane(new JSONRestDataPlaneConsumerProfiled(dataPort, endPoint, threads));

	consumer.connect();
    }
    
    
    public RestDataConsumer(String addr, int dataPort, String endPoint, byte conf) throws IOException {
	this(conf);

	// set up data plane
        
        if (doProfiling == 0) 
            consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort, endPoint));
        else
             consumer.setDataPlane(new JSONRestDataPlaneConsumerProfiled(addr, dataPort, endPoint));

	consumer.connect();
    }
    
    
    public RestDataConsumer(String addr, int dataPort, String endPoint, int threads, byte conf) throws IOException {
	this(conf);

	// set up data plane
        
        if (doProfiling == 0) 
            consumer.setDataPlane(new JSONRestDataPlaneConsumer(addr, dataPort, endPoint, threads));
        else
            consumer.setDataPlane(new JSONRestDataPlaneConsumerProfiled(addr, dataPort, endPoint, threads));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        byte conf = 1;
        int nThreads;
        
        String endPoint ="reporter";
        try {
            switch (args.length) {
                case 0:
                    new RestDataConsumer(port, endPoint, conf);
                    System.err.println("RestDataConsumer (conf=" + conf + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;
                case 3:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    sc = new Scanner(args[2]);
                    conf = sc.nextByte();
                    new RestDataConsumer(port, endPoint, conf);
                    System.err.println("RestDataConsumer (conf=" + conf + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
                    break;    
                case 4:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    endPoint = args[1];
                    sc = new Scanner(args[2]);
                    nThreads = sc.nextInt();
                    sc = new Scanner(args[3]);
                    conf = sc.nextByte();
                    new RestDataConsumer(port, endPoint, nThreads, conf);
                    System.err.println("RestDataConsumer (conf=" + conf + ") listening on http://*" + ":" + port + "/" + endPoint + "/");
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
                    conf = sc.nextByte();
                    new RestDataConsumer(bindAddress, port, endPoint, nThreads, conf);
                    System.err.println("RestDataConsumer (conf=" + conf + ") listening on http://" + bindAddress + ":" + port + "/" + endPoint + "/");
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
