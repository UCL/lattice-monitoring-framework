package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.im.dht.AbstractDHTRootInfoPlane;


/**
 * A TomP2PDHTRootInfoPlane is an InfoPlane implementation
 that acts as a ROOT for the Information Model data.
 * There needs to be one root for a DHT.
 * The other nodes connect to it.
 */
public class TomP2PDHTRootInfoPlane extends AbstractDHTRootInfoPlane {
    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     */
    
     AnnounceEventListener listener;
    
    public TomP2PDHTRootInfoPlane(String localHostname, int localPort) {
	rootHost = localHostname;
	rootPort = localPort;

        // from the super class
	imNode = new TomP2PIMNode(localPort, localHostname, localPort);
        imNode.addAnnounceEventListener(this.getInfoPlaneDelegate());
    } 
    
    public TomP2PDHTRootInfoPlane(int localPort) {
	rootPort = localPort;

        // from the super class
	imNode = new TomP2PIMNode(localPort);
        rootHost = imNode.getRemoteHostname();
        
        imNode.addAnnounceEventListener(this.getInfoPlaneDelegate());
    }
    
}