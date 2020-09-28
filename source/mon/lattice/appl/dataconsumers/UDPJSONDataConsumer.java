package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerJSON;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerJSONProfiled;

/**
 * A UDPJSONDataConsumer receives measurements from a UDP Data Plane.
 * It decodes JSON measurements and can either print them out or doing nothing
 */
public class UDPJSONDataConsumer {
    // The consumer
    AbstractLifecycleDataConsumer consumer;
    
    byte printOutput;
    byte doProfiling;
    
    
    private void parseConf(byte conf) {
        printOutput = (byte) (conf &  0x01);
        doProfiling = (byte) ((conf & 0x02) >>> 1);
    }

    /*
     * Construct a UDPJSONDataConsumer
     */
    
    private UDPJSONDataConsumer(byte conf) {
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
    
    
    
    public UDPJSONDataConsumer(int dataPort, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0)
            consumer.setDataPlane(new UDPDataPlaneConsumerJSON(dataPort));
        else
            consumer.setDataPlane(new UDPDataPlaneConsumerJSONProfiled(dataPort));

	consumer.connect();
    }
    
    
    public UDPJSONDataConsumer(String addr, int dataPort, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0)
            consumer.setDataPlane(new UDPDataPlaneConsumerJSON(address));
        else
            consumer.setDataPlane(new UDPDataPlaneConsumerJSONProfiled(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        byte conf = 1;
        
        try {
            switch (args.length) {
                case 0:
                    new UDPJSONDataConsumer(port, conf);
                    System.err.println("UDPJSONDataConsumer (conf=" + conf + ") listening on *:" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    conf = sc.nextByte();
                    new UDPJSONDataConsumer(port, conf);
                    System.err.println("UDPJSONDataConsumer (conf=" + conf + ") listening on *:" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    conf = sc.nextByte();
                    new UDPJSONDataConsumer(bindAddress, port, conf);
                    System.err.println("UDPJSONDataConsumer (conf=" + conf + ") listening on " + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: UDPJSONDataConsumer [port] [bind address] [0-3]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
