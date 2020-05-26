/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

/**
 *
 * @author uceeftu
 */

import mon.lattice.control.zmq.ZMQDataConsumerControlPlaneXDRConsumer;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumer;
import mon.lattice.im.zmq.ZMQDataConsumerInfoPlane;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.ID;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.appl.Daemon;


public class ZMQControllableDataConsumerDaemon extends Daemon {
    DefaultControllableDataConsumer consumer;
    
    String dataConsumerName = "controllable-DC";
    
    int dataPort;
    
    InetSocketAddress ctrlPair;
    
    String remoteInfoHost;
    int remoteInfoPort;
    
    
    public ZMQControllableDataConsumerDaemon(String myID,
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          String controlAddr,
                                          int controlPort
                                          ) throws UnknownHostException {
    
        super.entityID = myID;
        this.dataPort = dataPort;
        
        this.ctrlPair = new InetSocketAddress(InetAddress.getByName(controlAddr), controlPort);
        
        this.remoteInfoHost = infoPlaneRootName;
        this.remoteInfoPort = infoPlaneRootPort;
    }
    
    
    @Override
    public void init() throws IOException {
        entityType = "data-consumer-";
        classMetadata = ZMQControllableDataConsumerDaemon.class;
        attachShutDownHook();
        initLogger();
        
        consumer = new DefaultControllableDataConsumer(dataConsumerName, ID.fromString(entityID));
        
        LOGGER.info("Data Consumer ID: " + consumer.getID());
        LOGGER.info("Process ID: " + consumer.getMyPID());
        LOGGER.info("Connecting to the Info Plane using: " +  remoteInfoHost + ":" + remoteInfoPort);
        LOGGER.info("Connecting to the Control Plane using: " + ctrlPair.getHostName() + ":" + ctrlPair.getPort());
        
        // set up data plane listening on *:port
        consumer.setDataPlane(new ZMQDataPlaneConsumer(dataPort));
        
        InfoPlane infoPlane = new ZMQDataConsumerInfoPlane(remoteInfoHost, remoteInfoPort);
        
        
        ((DataConsumerInteracter) infoPlane).setDataConsumer(consumer);
        consumer.setInfoPlane(infoPlane);
        
        ControlPlane controlPlane;
        controlPlane = new ZMQDataConsumerControlPlaneXDRConsumer(ctrlPair);
        
        ((DataConsumerInteracter) controlPlane).setDataConsumer(consumer);
        consumer.setControlPlane(controlPlane);
    }
    
    
    @Override
    protected boolean connect() throws IOException {
        boolean connected = consumer.connect();
        if (connected) {
            LOGGER.info("Connected to the Info Plane using: " + consumer.getInfoPlane().getInfoRootHostname() + ":" + remoteInfoPort);
            return connected;
        } else {
            throw new IOException("Error while connecting to the Planes");
        } 
    }
    
    
   
    @Override
    public void run() {
        LOGGER.info("Disconnecting from the planes before shutting down");
        try {
            // performs deannounce and then disconnect for each plane
            consumer.disconnect(); 
        } catch (Exception e) {
            LOGGER.error("Something went wrong while Disconnecting from the planes " + e.getMessage());
          }
    }
    
    
    public static void main(String [] args) {
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


