package mon.lattice.appl.dataconsumers;

/**
 * A ZMQControllableDataConsumerWithConnectDaemon extends AbstractZMQControllableDataConsumerDaemon
 * with a ZMQ data plane implementation able to connect to a remote host and port.
 * 
 * @author uceeftu
 */

import mon.lattice.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerWithNames;


public class ZMQControllableDataConsumerWithConnectDaemon extends AbstractZMQControllableDataConsumerDaemon {
    
    String remoteDataHost;

    public ZMQControllableDataConsumerWithConnectDaemon(String myID, 
                                                        String remoteDataHost,
                                                        int dataPort, 
                                                        String infoPlaneRootName, 
                                                        int infoPlaneRootPort, 
                                                        String controlAddr, 
                                                        int controlPort) throws UnknownHostException {
        
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
        this.remoteDataHost = remoteDataHost;
    }

    /** Initialises the objects used for the info and control planes via calling
    * the parent class; then sets the data plane to ZMQ XDR with Names. 
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new ZMQDataPlaneConsumerWithNames(remoteDataHost, dataPort));
    }
    
    
    public static void main(String[] args) {
        try {
            String dcID = ID.generate().toString();
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controlEndPoint = null;
            int controlRemotePort = 5555;
            String remoteDataHost = null;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    remoteDataHost = infoHost = controlEndPoint = loopBack;
                    break;
                case 5:
                    remoteDataHost = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[1];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                case 6:
                    dcID = args[0];
                    remoteDataHost = args[1];
                    sc = new Scanner(args[2]);
                    dataPort = sc.nextInt();
                    infoHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                default:
                    LOGGER.error("usage: ControllableDataConsumerDaemon [dcID] localdataPort infoRemoteHost infoRemotePort controlLocalPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerWithConnectDaemon dataConsumer = new ZMQControllableDataConsumerWithConnectDaemon(dcID,
                                                                                   remoteDataHost,
                                                                                   dataPort, 
                                                                                   infoHost, 
                                                                                   infoRemotePort,
                                                                                   controlEndPoint, 
                                                                                   controlRemotePort);
            dataConsumer.init();
            dataConsumer.connect();
            
        } catch (IOException ex) {
            LOGGER.error("Error while starting the Data Consumer " + ex.getMessage());
            System.exit(1); //terminating as there was an error while connecting to the planes
        } 
    }
    

}


