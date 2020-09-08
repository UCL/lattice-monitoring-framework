package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSON;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONProfiled;

/**
 * A WebSocketDataConsumer receives measurements from a WS Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing
 */
public class WebSocketJSONDataConsumer {
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
    
    private WebSocketJSONDataConsumer(byte conf) {
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
    
    
    
    public WebSocketJSONDataConsumer(int dataPort, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSON(dataPort));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(dataPort));
        
	consumer.connect();
    }
    
    
    public WebSocketJSONDataConsumer(String addr, int dataPort, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSON(address));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(address));
        
	consumer.connect();
    }
    
    
    public WebSocketJSONDataConsumer(int dataPort, int nThreads, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSON(dataPort, nThreads));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(dataPort, nThreads));
        
	consumer.connect();
    }
    
    
    public WebSocketJSONDataConsumer(String addr, int dataPort, int nThreads, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSON(address, nThreads));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(address, nThreads));
        
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
                    new WebSocketJSONDataConsumer(port, conf);
                    System.err.println("WebSocketDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    conf = sc.nextByte();
                    new WebSocketJSONDataConsumer(port, conf);
                    System.err.println("WebSocketDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port);
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
                        new WebSocketJSONDataConsumer(bindAddress, port, conf);
                        System.err.println("WebSocketDataConsumer (conf=" + conf + ") listening on ws://" + bindAddress + ":" + port);
                    }
                    
                    else if (nThreads > 0) {
                        new WebSocketJSONDataConsumer(port, nThreads, conf);
                        System.err.println("WebSocketDataConsumer (conf=" + conf + ") listening on ws://*" + ":" + port + "(threads=" + nThreads + ")");
                    }
                    
                    break;
                default:
                    System.err.println("usage: WebSocketDataConsumer [port] [bind address | nThreads] [0-3]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
