// PS.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2020

package mon.lattice.appl.demo;

import mon.lattice.appl.probes.host.linux.ProcessInfo;
import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.distribution.multicast.MulticastDataPlaneProducerWithNames;
import mon.lattice.distribution.multicast.MulticastAddress;
import mon.lattice.control.udp.AbstractUDPControlPlaneConsumer;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends PS info and uses a Multicast Data Plane.
 */
public class PS {
    // The DataSource
    DataSource ds;

    int pid;
    
    /*
     * Construct a PS.
     */
    public PS(int pid, String addr, int dataPort, String myHostname) {
        this.pid = pid;

	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducerWithNames(address));
        //ds.setControlPlane(new AbstractUDPControlPlaneConsumer());

	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.addProbe(p);
	ds.deactivateProbe(p);
    }

    public static void main(String [] args) {
	String addr = "229.229.0.1";
	int port = 2299;

        int pid = 0;

	if (args.length == 1) {
	    // use address existing settings
	    Scanner sc = new Scanner(args[0]);
	    pid = sc.nextInt();

            
	} else if (args.length == 3) {
	    Scanner sc = new Scanner(args[0]);
	    pid = sc.nextInt();

	    addr = args[1];

	    sc = new Scanner(args[2]);
	    port = sc.nextInt();


	} else {
	    System.err.println("PS pid [multicast-address port]");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

        if (pid != 0) {
            // we got a hostname
            PS ps = new PS(pid, addr, port, currentHost);

            ps.turnOnProbe(new ProcessInfo(pid, currentHost + ".cpuInfo"));
        } else {
            System.err.println("No pid");
	    System.err.println("PS pid [multicast-address port]");
	    System.exit(1);
        }
    }


}
