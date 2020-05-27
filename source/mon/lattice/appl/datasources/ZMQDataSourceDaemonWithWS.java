/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.datasources;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.core.ID;
import mon.lattice.distribution.ws.WSDataPlaneProducerWithNames;


public class ZMQDataSourceDaemonWithWS extends AbstractZMQDataSourceDaemon {

    public ZMQDataSourceDaemonWithWS(String myID, String myDSName, String dataConsumerName, int dataConsumerPort, String infoPlaneRootName, int infoPlaneRootPort, String controlHostAddress, int controlHostPort) throws UnknownHostException {
        super(myID, myDSName, dataConsumerName, dataConsumerPort, infoPlaneRootName, infoPlaneRootPort, controlHostAddress, controlHostPort);
    }

    @Override
    public void init() throws IOException {
        super.init();
        dataSource.setDataPlane(new WSDataPlaneProducerWithNames(dataConsumerPair)); 
    }
    
    
    public static void main(String[] args) {
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
                    System.err.println("use: ZMQDataSourceDaemonWithWS [UUID] dcAddress dcPort infoHost infoPort controllerHost controllerPort");
                    System.exit(1);
            }
            
            ZMQDataSourceDaemonWithWS dataSourceDaemon = new ZMQDataSourceDaemonWithWS(
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
