// WSConsumerWSForwarder1.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.core.Reporter;
import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.io.IOException;


/**
 * This receives measurements from a WebSocket Data Plane
 * and then re-encodes the measurements as JSON
 * to forward over another websocket.
 */
public class WSConsumerWSForwarder1 {
    // The Basic consumer
    DataConsumerWSForwarder consumer;

    // Listen address
    InetSocketAddress address;

    // Forward address
    InetSocketAddress forwardInetAddress;

    /*
     * Construct a WSConsumerWSForwarder1
     */
    public WSConsumerWSForwarder1(String addr, int dataPort, String forwardAddr, int forwardPort) throws IOException {
	// set up an IP address for listening for data
	address = new InetSocketAddress(addr, dataPort);

        // set up forwarding address
        forwardInetAddress = new InetSocketAddress(forwardAddr, forwardPort);

        System.err.println("Set up DataConsumerWSForwarder");
            
        // set up a special forwarding consumer
        consumer = new DataConsumerWSForwarder("WSConsumerWSForwarderConsumer", forwardInetAddress);

        
        System.err.println("Set up WSDataPlaneConsumerWithNames");
            
        // set up data plane
        consumer.setDataPlane(new WSDataPlaneConsumerWithNames(address));

        consumer.connect();

        try {
            System.err.println("Allow forwarding DataConsumerWSForwarder");
            
            consumer.startForwarding();
       } catch (Exception ioe) {
            System.err.println("Forwarder problem at " + forwardInetAddress + ". probably not listening");
            throw new Error();
        }
        

    }

    public static void main(String [] args) throws IOException {
	if (args.length == 0) {
	    System.err.println("WSConsumerWSForwarder1 listening on localhost/22996");
	    System.err.println("WSConsumerWSForwarder1 forwarding to localhost/22997");

	    new WSConsumerWSForwarder1("localhost", 22996, "localhost", 22997);
            
	} else if (args.length == 4) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    String forwardAddr = args[2];

            sc = new Scanner(args[3]);
	    int forwardPort = sc.nextInt();

	    new WSConsumerWSForwarder1(addr, port, forwardAddr, forwardPort);

	    System.err.println("WSConsumerWSForwarder1 listening on " + addr + "/" + port + " forwarding to " + forwardAddr + "/" + forwardPort);
	} else {
	    System.err.println("usage: WSConsumerWSForwarder1 localhost port forwardAddr forwardPort");
	    System.exit(1);
	}
    }

}

