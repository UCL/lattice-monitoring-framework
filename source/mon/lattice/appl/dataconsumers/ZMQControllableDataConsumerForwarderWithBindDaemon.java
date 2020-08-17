package mon.lattice.appl.dataconsumers;

/**
 * ZMQControllableDataConsumerForwarderWithBindDaemon is a special consumer
 * that has a ZMQ Data Plane implementation able to receive and forward measurement
 * without the need of intermediate decode / encode. 
 * This particular implementation receives as parameter the port on which the PUB 
 * socket used as forwarder will bind.
 * 
 * @author uceeftu
 */

import mon.lattice.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerAndForwarder;


public class ZMQControllableDataConsumerForwarderWithBindDaemon extends AbstractZMQControllableDataConsumerDaemon {
    
    int localForwardingPort;

    public ZMQControllableDataConsumerForwarderWithBindDaemon(String myID, 
                                                                 int dataPort, 
                                                                 String infoPlaneRootName, 
                                                                 int infoPlaneRootPort, 
                                                                 String controlAddr, 
                                                                 int controlPort,
                                                                 int localForwardingPort) throws UnknownHostException {
        
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
        this.localForwardingPort = localForwardingPort;
    }

    /** Initialises the objects used for the info and control planes via calling
    * the parent class; then sets the data plane to ZMQ XDR with Names. 
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new ZMQDataPlaneConsumerAndForwarder(dataPort, localForwardingPort));
    }
    
    
    public static void main(String[] args) {
        try {
            String dcID = ID.generate().toString();
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controlEndPoint = null;
            int controlRemotePort = 5555;
            int localForwardingPort = dataPort + 1; // by default listen on dataport + 1
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    infoHost = controlEndPoint = loopBack;
                    break;
                case 5:
                    sc = new Scanner(args[0]);
                    dataPort = sc.nextInt();
                    infoHost = args[1];
                    sc = new Scanner(args[2]);
                    infoRemotePort = sc.nextInt();
                    sc = new Scanner(args[3]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    sc = new Scanner(args[4]);
                    localForwardingPort = sc.nextInt();
                    break;
                case 6:
                    dcID = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    sc = new Scanner(args[5]);
                    localForwardingPort = sc.nextInt();
                    break;
                default:
                    LOGGER.error("usage: ZMQControllableDataConsumerForwarderWithBindDaemon [dcID] localdataPort infoRemoteHost infoRemotePort controlLocalPort localForwardingPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerForwarderWithBindDaemon dataConsumer = new ZMQControllableDataConsumerForwarderWithBindDaemon(dcID, 
                                                                                   dataPort, 
                                                                                   infoHost, 
                                                                                   infoRemotePort,
                                                                                   controlEndPoint, 
                                                                                   controlRemotePort,
                                                                                   localForwardingPort);
            dataConsumer.init();
            dataConsumer.connect();
            
        } catch (IOException ex) {
            LOGGER.error("Error while starting the Data Consumer " + ex.getMessage());
            System.exit(1); //terminating as there was an error while connecting to the planes
        } 
    }
    

}