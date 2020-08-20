// TcpdumpProducer.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
//import mon.lattice.distribution.ws.WSDataPlaneProducerJSON;
import mon.lattice.distribution.ws.WSDataPlaneProducerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.net.UnknownHostException;

/**
 * This monitor sends tcpdump data and uses a WebSocket Data Plane.
 */
public class TcpdumpProducer {
    // The DataSource
    DataSource ds;

    Probe pb;
    
    /*
     * Construct a TcpdumpProducer.
     */
    public TcpdumpProducer(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up  address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new WSDataPlaneProducerWithNames(address));

        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Shutting down ...");
                    //some cleaning up code...
                    turnOffProbe(pb);
                    ds.disconnect();
                }
            });
        
	ds.connect();
    }

    private void turnOnProbe(Probe p) {
        pb = p;
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.deactivateProbe(p);
	ds.removeProbe(p);
        pb = null;
    }

    public static void main(String [] args) {
	String addr = "localhost";
	int port = 22997;
        String tcpdump_interface = "lo0";
        int tcpdump_port = 80;

	if (args.length == 2) {
	    // use existing settings
            // get tcpdump-interface tcpdump-port
	    tcpdump_interface = args[0];

	    Scanner sc = new Scanner(args[1]);
	    tcpdump_port = sc.nextInt();

            
	} else if (args.length == 4) {
            // get tcpdump-interface tcpdump-port
	    tcpdump_interface = args[0];

	    Scanner sc = new Scanner(args[1]);
	    tcpdump_port = sc.nextInt();

	    addr = args[2];

            sc = new Scanner(args[3]);
	    port = sc.nextInt();

	} else {
	    System.err.println("TcpdumpProducer tcpdump-interface tcpdump-port [address port]");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	}

	// we got a hostname
	TcpdumpProducer producer = new TcpdumpProducer(addr, port, currentHost);

	Probe probe = new TcpdumpProbe(currentHost, tcpdump_interface, tcpdump_port, 5);

	//probe.setServiceID(new ID(12345));
	//probe.setGroupID(new ID(2));

	producer.turnOnProbe(probe);

    }


}
