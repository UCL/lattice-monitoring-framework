package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONProfiled;
import mon.lattice.distribution.ws.WSDataPlaneConsumerJSONSimple;

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
            consumer = new VoidConsumer();
        }
        
        else {
            // set up a BasicConsumer (with a built-in PrintReporter)
            consumer = new BasicConsumer();
        }
    }
    
    
    
    public WebSocketJSONSimpleDataConsumer(int dataPort, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(dataPort));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(dataPort));
        
	consumer.connect();
    }
    
    
    public WebSocketJSONSimpleDataConsumer(String addr, int dataPort, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0)
            consumer.setDataPlane(new WSDataPlaneConsumerJSONSimple(address));
        else
            consumer.setDataPlane(new WSDataPlaneConsumerJSONProfiled(address));
        
	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
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
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    conf = sc.nextByte();
                    new WebSocketJSONSimpleDataConsumer(bindAddress, port, conf);
                    System.err.println("WebSocketJSONSimpleDataConsumer (conf=" + conf + ") listening on ws://" + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: WebSocketJSONSimpleDataConsumer [port] [bind address] [0-3]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
