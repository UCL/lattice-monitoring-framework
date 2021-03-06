package mon.lattice.appl.dataconsumers;

/**
 * A ZMQControllableDataConsumerDaemon extends AbstractZMQControllableDataConsumerDaemon
 * with a ZMQ data plane implementation listening on a given port.
 * 
 * @author uceeftu
 */

import mon.lattice.distribution.zmq.ZMQDataPlaneConsumerWithNames;
import mon.lattice.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ZMQControllableDataConsumerDaemon extends AbstractZMQControllableDataConsumerDaemon {

    public ZMQControllableDataConsumerDaemon(String myID, int dataPort, String infoPlaneRootName, int infoPlaneRootPort, String controlAddr, int controlPort) throws UnknownHostException {
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
    }

    /** Initialises the objects used for the info and control planes via calling
    * the parent class; then sets the data plane to ZMQ XDR with Names. 
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new ZMQDataPlaneConsumerWithNames(dataPort));
    }
    
    
    public static void main(String[] args) {
        try {
            String dcID = ID.generate().toString();
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controlEndPoint = null;
            int controlRemotePort = 5555;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    infoHost = controlEndPoint = loopBack;
                    break;
                case 4:
                    sc = new Scanner(args[0]);
                    dataPort = sc.nextInt();
                    infoHost = args[1];
                    sc = new Scanner(args[2]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[3]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                case 5:
                    dcID = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                default:
                    LOGGER.error("usage: ControllableDataConsumerDaemon [dcID] localdataPort infoRemoteHost infoRemotePort controlLocalPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerDaemon dataConsumer = new ZMQControllableDataConsumerDaemon(dcID, 
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


