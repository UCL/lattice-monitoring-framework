package mon.lattice.appl.dataconsumers;

/**
 * ZMQControllableDataConsumerForwarderWithConnectDaemon is a special consumer
 * that has a ZMQ Data Plane implementation able to receive and forward measurement
 * without the need of intermediate decode / encode. 
 * This particular implementation receives as parameter the address and port to which the PUB 
 * socket will connect.
 * 
 * @author uceeftu
 */

import mon.lattice.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerAndForwarder;


public class ZMQControllableDataConsumerForwarderWithConnectDaemon extends AbstractZMQControllableDataConsumerDaemon {
    
    String remoteForwardingHost;
    int remoteForwardingPort;

    public ZMQControllableDataConsumerForwarderWithConnectDaemon(String myID, 
                                                                 int dataPort, 
                                                                 String infoPlaneRootName, 
                                                                 int infoPlaneRootPort, 
                                                                 String controlAddr, 
                                                                 int controlPort,
                                                                 String remoteForwardingHost,
                                                                 int remoteForwardingPort) throws UnknownHostException {
        
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
        this.remoteForwardingHost = remoteForwardingHost;
        this.remoteForwardingPort = remoteForwardingPort;
    }

    /** Initialises the objects used for the info and control planes via calling
    * the parent class; then sets the data plane to ZMQ XDR with Names. 
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new ZMQDataPlaneConsumerAndForwarder(dataPort, remoteForwardingHost, remoteForwardingPort));
    }
    
    
    public static void main(String[] args) {
        try {
            String dcID = ID.generate().toString();
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controlEndPoint = null;
            int controlRemotePort = 5555;
            String remoteForwardingAddress = null;
            int remoteForwardingPort = dataPort + 1; // by default we forward to localhost:dataPort+1
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    infoHost = controlEndPoint = remoteForwardingAddress = loopBack;
                    break;
                case 6:
                    sc = new Scanner(args[0]);
                    dataPort = sc.nextInt();
                    infoHost = args[1];
                    sc = new Scanner(args[2]);
                    infoRemotePort = sc.nextInt();
                    sc = new Scanner(args[3]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    remoteForwardingAddress = args[4];
                    sc = new Scanner(args[5]);
                    remoteForwardingPort = sc.nextInt();
                    break;
                case 7:
                    dcID = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    remoteForwardingAddress = args[5];
                    sc = new Scanner(args[6]);
                    remoteForwardingPort = sc.nextInt();
                    break;
                default:
                    LOGGER.error("usage: ZMQControllableDataConsumerForwarderWithConnectDaemon [dcID] localdataPort infoRemoteHost infoRemotePort controlLocalPort remoteForwardingAddress remoteForwardingPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerForwarderWithConnectDaemon dataConsumer = new ZMQControllableDataConsumerForwarderWithConnectDaemon(dcID, 
                                                                                   dataPort, 
                                                                                   infoHost, 
                                                                                   infoRemotePort,
                                                                                   controlEndPoint, 
                                                                                   controlRemotePort,
                                                                                   remoteForwardingAddress,
                                                                                   remoteForwardingPort);
            dataConsumer.init();
            dataConsumer.connect();
            
        } catch (IOException ex) {
            LOGGER.error("Error while starting the Data Consumer " + ex.getMessage());
            System.exit(1); //terminating as there was an error while connecting to the planes
        } 
    }
    

}