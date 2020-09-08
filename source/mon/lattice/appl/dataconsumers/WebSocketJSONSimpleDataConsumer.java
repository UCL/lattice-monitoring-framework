package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONProfiled;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONSimple;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONSimpleProfiled;

/**
 * A WebSocketJSONSimpleDataConsumer receives measurements from a WS Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing. 
 * It uses the json-simple library.
 */
public class WebSocketJSONSimpleDataConsumer {
    // The consumer
    AbstractLifecycleDataConsumer consumer;
    
    byte printOutput;
    byte doProfiling;

    
    private void parseConf(byte conf) {
        printOutput = (byte) (conf &  0x01);
        doProfiling = (byte) ((conf & 0x02) >>> 1);
    }
    
    /*
     * Construct a WebSocketJSONDataConsumer
     */
    
    private WebSocketJSONSimpleDataConsumer(byte conf) {
        parseConf(conf);
        
        if (printOutput == 0) {
            // set up a VoidConsumer (no print)
            System.err.println("Printing is disabled");
            consumer = new VoidConsumer();
        }
        
        else {
            // set up a BasicConsumer (with a built-in PrintReporter)
            System.err.println("Printing is enabled");
            consumer = new BasicConsumer();
        }
    }
    
    
    
    public WebSocketJSONSimpleDataConsumer(int dataPort, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0) {
            System.err.println("Profiling is disabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(dataPort));
        }
        else {
            System.err.println("Profiling is enabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimpleProfiled(dataPort));
        }
        
	consumer.connect();
    }
    
    
    public WebSocketJSONSimpleDataConsumer(String addr, int dataPort, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0) {
            System.err.println("Profiling is disabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(address));
        }
        else {
            System.err.println("Profiling is enabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimpleProfiled(address));
        }
        
	consumer.connect();
    }
    
    
    public WebSocketJSONSimpleDataConsumer(int dataPort, int nThreads, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0) {
            System.err.println("Profiling is disabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(dataPort, nThreads));
        }
        else {
            System.err.println("Profiling is enabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimpleProfiled(dataPort, nThreads));
        }
        
	consumer.connect();
    }
    
    
    public WebSocketJSONSimpleDataConsumer(String addr, int dataPort, int nThreads, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0) {
            System.err.println("Profiling is disabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(address, nThreads));
        }
        else {
            System.err.println("Profiling is enabled");
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(address, nThreads));
        }
        
	consumer.connect();
    }
    
    
    

    public static void main(String [] args) {
        String bindAddress=null;
        int port = 9999;
        int nThreads=0;
        byte conf = 1;
        
        try {
            switch (args.length) {
                case 0:
                    new WebSocketJSONSimpleDataConsumer(port, conf);
                    System.err.println("WebSocketJSONSimpleDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    conf = sc.nextByte();
                    new WebSocketJSONSimpleDataConsumer(port, conf);
                    System.err.println("WebSocketJSONSimpleDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    
                    sc = new Scanner(args[1]);
                    if (sc.hasNextInt())
                        nThreads = sc.nextInt();
                    else
                        bindAddress = args[1];
                    
                    sc = new Scanner(args[2]);
                    conf = sc.nextByte();
                    
                    if (bindAddress != null) {
                        new WebSocketJSONSimpleDataConsumer(bindAddress, port, conf);
                        System.err.println("WebSocketJSONSimpleDataConsumer (conf=" + conf + ") listening on ws://" + bindAddress + ":" + port);
                    }
                    
                    else if (nThreads > 0) {
                        new WebSocketJSONSimpleDataConsumer(port, nThreads, conf);
                        System.err.println("WebSocketJSONSimpleDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port + "(threads=" + nThreads + ")");
                    }
                    
                    break;
                default:
                    System.err.println("usage: WebSocketJSONSimpleDataConsumer [port] [bind address | nThreads] [0-3]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
