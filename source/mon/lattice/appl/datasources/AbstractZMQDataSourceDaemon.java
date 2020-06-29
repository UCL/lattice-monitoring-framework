package mon.lattice.appl.datasources;

import mon.lattice.appl.Daemon;
import mon.lattice.control.zmq.ZMQDataSourceControlPlaneXDRConsumer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ID;
import mon.lattice.im.zmq.ZMQDataSourceInfoPlane;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * This DataSource in a basic control point for probes that uses a ZMQ Control Plane and an Info Plane and 
 * logs out/err to a file rather than standard streams. 
 * The data plane implementation is left to the sub classes.
 **/

public abstract class AbstractZMQDataSourceDaemon extends Daemon {
    protected ControllableDataSource dataSource;
    
    String dataSourceName;
    
    InetSocketAddress dataConsumerPair;
    InetSocketAddress ctrlPair;
    InetSocketAddress remoteCtrlPair;
    
    String remoteInfoHost;
    int remoteInfoPort;
    
    /**
     * Construct a ZMQDataSourceDaemon with no pre-loaded probes running as a daemon 
     * @param myID the UUID of the Data Source
     * @param myDSName the Name of the Data Source
     * @param dataConsumerName the Data Consumer to connect to
     * @param dataConsumerPort the port of the Data Consumer to connect to
     * @param infoPlaneRootName the host name of the Info Plane node to connect/bootstrap to (i.e., the Controller)
     * @param infoPlaneRootPort the port of the Info Plane node to connect/bootstrap to (i.e., the Controller)
     * @param controlHostAddress the Controller address
     * @param controlHostPort the Controller port
     * @throws UnknownHostException
     **/
    
    public AbstractZMQDataSourceDaemon(
                           String myID,
                           String myDSName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           String controlHostAddress,
                           int controlHostPort
                           ) throws UnknownHostException {
    
    
        this.entityID = myID;
        this.dataSourceName = myDSName;
        
        this.dataConsumerPair = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);
        this.ctrlPair = new InetSocketAddress(InetAddress.getByName(controlHostAddress), controlHostPort);
        
        this.remoteInfoHost = infoPlaneRootName;
        this.remoteInfoPort = infoPlaneRootPort;
    }
     


    @Override
    public void init() throws IOException {
        entityType = "data-source-";
        classMetadata = AbstractZMQDataSourceDaemon.class;
        
        attachShutDownHook();
        initLogger();
        
	dataSource = new ZMQControllableDataSource(dataSourceName, ID.fromString(entityID));
        
        LOGGER.info("Data Source ID: " + dataSource.getID());
        LOGGER.info("Process ID: " + dataSource.getMyPID());
        LOGGER.info("Using Data Source name: " + dataSourceName);
        LOGGER.info("Sending measurements to Data Consumer: " + dataConsumerPair.getHostName() + ":" + dataConsumerPair.getPort());
        LOGGER.info("Connecting to the Control Plane: " + ctrlPair.getHostName() + ":" + ctrlPair.getPort());
        
        // ZMQ Info Plane
        dataSource.setInfoPlane(new ZMQDataSourceInfoPlane(remoteInfoHost, remoteInfoPort));
            
        // ZMQ Control Plane   
        dataSource.setControlPlane(new ZMQDataSourceControlPlaneXDRConsumer(ctrlPair));
    }

    @Override
    protected boolean connect() throws IOException {
        boolean connected = dataSource.connect();
        if (connected) {
            LOGGER.info("Connected to the Info Plane using: " + dataSource.getInfoPlane().getInfoRootHostname() + ":" + remoteInfoPort);
            return connected;
        } else {
            throw new IOException("Error while connecting to the Planes");
        } 
    }
    
    
    @Override
    public void run() {
        LOGGER.info("Disconnecting from the planes before shutting down");
        try {
            // will first do deannounce and then disconnect from each of the planes
            dataSource.disconnect();
        } catch (Exception e) {
            LOGGER.error("Something went wrong while disconnecting from the planes " + e.getMessage());
          }
    }
}
