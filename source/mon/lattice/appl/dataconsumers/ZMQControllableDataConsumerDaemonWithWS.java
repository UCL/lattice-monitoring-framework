/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.core.ID;
import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;


public class ZMQControllableDataConsumerDaemonWithWS extends ZMQControllableDataConsumerDaemon {

    public ZMQControllableDataConsumerDaemonWithWS(String myID, int dataPort, String infoPlaneRootName, int infoPlaneRootPort, String controlAddr, int controlPort) throws UnknownHostException {
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
    }

    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new WSDataPlaneConsumerWithNames(dataPort));
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
                    LOGGER.error("usage: ControllableDataConsumerDaemonWithWS [dcID] localdataPort infoRemoteHost infoRemotePort controlLocalPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerDaemonWithWS dataConsumer = new ZMQControllableDataConsumerDaemonWithWS(dcID, 
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
