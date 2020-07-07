package mon.lattice.appl.dataconsumers;

/**
 *
 * @author uceeftu
 */

import mon.lattice.control.zmq.ZMQDataConsumerControlPlaneXDRConsumer;
import mon.lattice.im.zmq.ZMQDataConsumerInfoPlane;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.ID;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import mon.lattice.appl.Daemon;


public abstract class AbstractZMQControllableDataConsumerDaemon extends Daemon {
    DefaultControllableDataConsumer consumer;
    
    String dataConsumerName = "controllable-DC";
    
    int dataPort;
    
    InetSocketAddress ctrlPair;
    
    String remoteInfoHost;
    int remoteInfoPort;
    
    
    public AbstractZMQControllableDataConsumerDaemon(String myID,
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
    
    
    /** Initialises the objects utilised for the info and control planes.
    * The measurements distribution (data plane) should be specified in the
    * method implementation of the subclasses.
    * 
    * @throws IOException 
    */
    @Override
    public void init() throws IOException {
        entityType = "data-consumer-";
        classMetadata = AbstractZMQControllableDataConsumerDaemon.class;
        attachShutDownHook();
        initLogger();
        
        consumer = new DefaultControllableDataConsumer(dataConsumerName, ID.fromString(entityID));
        
        LOGGER.info("Data Consumer ID: " + consumer.getID());
        LOGGER.info("Process ID: " + consumer.getMyPID());
        LOGGER.info("Connecting to the Info Plane using: " +  remoteInfoHost + ":" + remoteInfoPort);
        LOGGER.info("Connecting to the Control Plane using: " + ctrlPair.getHostName() + ":" + ctrlPair.getPort());
        
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
    
    
}


