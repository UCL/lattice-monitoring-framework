package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import mon.lattice.core.AbstractLifecycleDataConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerNoNames;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerNoNamesProfiled;

/**
 * A UDPXDRDataConsumer receives measurements from a UDP Data Plane.
 * It decodes XDR measurements (without names) and can either print them out or doing nothing
 */
public class UDPXDRDataConsumer {
    // The consumer
    AbstractLifecycleDataConsumer consumer;
    
    byte printOutput;
    byte doProfiling;
    
    
    private void parseConf(byte conf) {
        printOutput = (byte) (conf &  0x01);
        doProfiling = (byte) ((conf & 0x02) >>> 1);
    }

    /*
     * Construct a UDPXDRDataConsumer
     */
    
    private UDPXDRDataConsumer(byte conf) {
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
    
    
    
    public UDPXDRDataConsumer(int dataPort, byte conf) throws IOException {    
        this(conf);
        
	// set up data plane
        if (doProfiling == 0)
            consumer.setDataPlane(new UDPDataPlaneConsumerNoNames(dataPort));
        else
            consumer.setDataPlane(new UDPDataPlaneConsumerNoNamesProfiled(dataPort));

	consumer.connect();
    }
    
    
    public UDPXDRDataConsumer(String addr, int dataPort, byte conf) throws IOException {
	this(conf);
        
	// set up data plane
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        
        if (doProfiling == 0)
            consumer.setDataPlane(new UDPDataPlaneConsumerNoNames(address));
        else
            consumer.setDataPlane(new UDPDataPlaneConsumerNoNamesProfiled(address));

	consumer.connect();
    }

    public static void main(String [] args) {
        String bindAddress;
        int port = 9999;
        byte conf = 1;
        
        try {
            switch (args.length) {
                case 0:
                    new UDPXDRDataConsumer(port, conf);
                    System.err.println("UDPXDRDataConsumer (conf=" + conf + ") listening on *:" + port);
                    break;
                case 2:
                    Scanner sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    sc = new Scanner(args[1]);
                    conf = sc.nextByte();
                    new UDPXDRDataConsumer(port, conf);
                    System.err.println("UDPXDRDataConsumer (conf=" + conf + ") listening on *:" + port);
                    break;
                case 3:
                    sc = new Scanner(args[0]);
                    port = sc.nextInt();
                    bindAddress = args[1];
                    sc = new Scanner(args[2]);
                    conf = sc.nextByte();
                    new UDPXDRDataConsumer(bindAddress, port, conf);
                    System.err.println("UDPXDRDataConsumer (conf=" + conf + ") listening on " + bindAddress + ":" + port);
                    break;
                default:
                    System.err.println("usage: UDPXDRDataConsumer [port] [bind address] [0-3]");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
