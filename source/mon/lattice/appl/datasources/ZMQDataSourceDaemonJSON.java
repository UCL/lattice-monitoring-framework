package mon.lattice.appl.datasources;

import mon.lattice.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.distribution.zmq.ZMQDataPlaneProducerJSON;



public class ZMQDataSourceDaemonJSON extends AbstractZMQDataSourceDaemon {

    public ZMQDataSourceDaemonJSON(String myID, String myDSName, String dataConsumerName, int dataConsumerPort, String infoPlaneRootName, int infoPlaneRootPort, String controlHostAddress, int controlHostPort) throws UnknownHostException {
        super(myID, myDSName, dataConsumerName, dataConsumerPort, infoPlaneRootName, infoPlaneRootPort, controlHostAddress, controlHostPort);
    }
    

    /** Initialises the objects used for the info and control planes via calling
    * the parent class; then sets the data plane to ZMQ JSON (with Names). 
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        super.init();
        dataSource.setDataPlane(new ZMQDataPlaneProducerJSON(dataConsumerPair));
    }
    
    
    public static void main(String [] args) {
        try {
            String dsID = ID.generate().toString();
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controllerHost = null;
            int controllerPort = 5555;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    dsName = dataConsumerAddr = controllerHost = loopBack;
                    infoHost = InetAddress.getLocalHost().getHostName();
                    break;
                case 5:
                    dataConsumerAddr = args[0];
                    sc = new Scanner(args[1]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    break;
                case 6:
                    dsID = args[0];
                    dataConsumerAddr = args[1];
                    sc = new Scanner(args[2]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    break;
                default:
                    System.err.println("use: ZMQDataSourceDaemon [UUID] dcAddress dcPort infoHost infoPort controllerHost controllerPort");
                    System.exit(1);
            }
            
            ZMQDataSourceDaemonJSON dataSourceDaemon = new ZMQDataSourceDaemonJSON(
                                                            dsID,
                                                            dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort,
                                                            controllerHost, 
                                                            controllerPort);
            dataSourceDaemon.init();
            dataSourceDaemon.connect();
            
        } catch (IOException ex) {
            LOGGER.error("Error while starting the Data Source " + ex.getMessage());
            System.exit(1); //terminating as there was an error while connecting to the planes
	}
    }
}
