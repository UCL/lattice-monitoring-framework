package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.ID;
import java.io.Serializable;
import java.io.IOException;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import java.net.InetAddress;
import mon.lattice.im.dht.AbstractDHTIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An TomP2PIMNode is responsible for converting  DataSource, ControllableDataConsumer and Probe
 attributes into Hashtable keys and values for the TomP2PDistributedHashTable.
 * <p>
 * For example, with a given DataSource you get:
 * <ul>
 * <li> /datasource/datasource-id/attribute = value
 * </ul>
 * and with a given Probe you get:
 * <ul>
 * <li> /probe/probe-id/attribute = value
 * </ul>
 */
public class TomP2PIMNode extends AbstractDHTIMNode implements AnnounceEventListener {
    // The actual DHT
    TomP2PDistributedHashTable dht = null;

    // the local port
    int localPort = 0;
    
    int localUDPPort = 0;
    int localTCPPort = 0;
    
    // the remote port
    int remotePort = 0;
    
    static Logger LOGGER = LoggerFactory.getLogger(TomP2PIMNode.class);

    /**
     * Construct an IMNode, given a local port and a remote host
     * and a remote port.
     */
    public TomP2PIMNode(int myPort, String remHost, int remPort) {
	localPort = myPort;
	remoteHost = remHost;
	remotePort = remPort;
    }
    
    /**
     * Construct an IMNode, given local TCP and UDP ports and a remote port.
     */
    
    public TomP2PIMNode(int myUDPPort, int myTCPPort, int remPort) {
	localUDPPort = myUDPPort;
        localTCPPort = myTCPPort;
        
	remotePort = remPort;
        remoteHost = null; // will be initialized after connection
    }
    
    
    /**
     * Construct an IMNode, given local TCP and UDP ports and a remote port.
     */
    
    public TomP2PIMNode(int myUDPPort, int myTCPPort, String remHost, int remPort) {
	localUDPPort = myUDPPort;
        localTCPPort = myTCPPort;
        remoteHost = remHost;
        
	remotePort = remPort;
    }
    
    /**
     * Construct an IMNode, given a local port and a remote port.
     */
    
    public TomP2PIMNode(int myPort, int remPort) {
	localPort = myPort;
	remotePort = remPort;
        remoteHost = null; // will be initialized after connection
    }
    
    public TomP2PIMNode(int myPort) {
	localPort = myPort;
	remotePort = localPort;
        remoteHost = null; // will be initialized after connection
    }

    /**
     * Connect to the DHT peers.
     */
    @Override
    public boolean connect() {
        String remoteConnectedHost = null;
        
	try {
	    // only connect if we don't already have a DHT
	    if (dht == null) {
                if (localPort == remotePort) {
                    dht = new TomP2PDistributedHashTable(localPort);
                    remoteHost = dht.connect();
                }
                else {
                    if (localPort != 0)
                        dht = new TomP2PDistributedHashTable(localPort, InetAddress.getLocalHost());
                    else
                        dht = new TomP2PDistributedHashTable(localUDPPort, localTCPPort, InetAddress.getLocalHost());
                    if (remoteHost == null)
                       remoteConnectedHost = dht.connect(remotePort);
                    else
                       remoteConnectedHost = dht.connect(remoteHost, remotePort);
                }

                //setting this TomP2PIMNode as a AnnounceEventListener in the DHT
                dht.addAnnounceEventListener(this);
                
		return remoteConnectedHost != null;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    LOGGER.error("Connect failed: " + ioe);
	    if (dht != null) {
		try {
		    dht.close();
		} catch (IOException e) {
		}
		dht = null;
	    }
	    return false;
	}
    }

    /**
     * Disconnect from the DHT peers.
     */
    @Override
    public boolean disconnect() {
        if (dht != null) {
            try {
                dht.close();
                dht = null;
                return true;
            } catch (IOException e) {
                dht = null;
                return false;
            }
        }
        // was already disconnected so returning true anyway
        return true;
    }

    
    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeout) {
        try {
            String newKey = "/datasource/" + dataSourceID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("ContainsDataSource failed for DS " + dataSourceID + " " + ioe.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
        try {
            String newKey = "/dataconsumer/" + dataConsumerID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("ContainsDataConsumer failed for DS " + dataConsumerID + " " + ioe.getMessage());
            return false;
        }
    }

    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeout) {
        try {
            String newKey = "/controlleragent/" + controllerAgentID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsControllerAgent failed for agent " + controllerAgentID + " " + ioe.getMessage());
            return false;
        }     
    }
    
    
    @Override
    public boolean containsProbe(ID probeID, int timeout) {
        try {
            String newKey = "/probe/" + probeID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsProbe failed for probe " + probeID);
            return false;
        }
    }
    
    
    

    /**
     * Put stuff into DHT.
     */
    @Override
    public boolean putDHT(String aKey, Serializable aValue) {
	try {
	    LOGGER.info("put " + aKey + " => " + aValue);
	    dht.put(aKey, aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("putDHT failed for key: '" + aKey + "' value: '" + aValue + "' " +ioe.getMessage());
	    return false;
	}
    }

    /**
     * Lookup info directly from the DHT.
     * @return the value if found, null otherwise
     */
    @Override
    public Object getDHT(String aKey) {
	try {
	    Object aValue = dht.get(aKey);
	    LOGGER.debug("get " + aKey +  " => " + aValue);
	    return aValue;
	} catch (IOException | ClassNotFoundException e) {
	    LOGGER.error("getDHT failed for key: '" + aKey + " " + e.getMessage());
	    return null;
	}
    }

    /**
     * Remove info from the DHT.
     * @return boolean
     */
    @Override
    public boolean remDHT(String aKey) {
	try {
	    dht.remove(aKey);
	    LOGGER.debug("removing " + aKey);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("remDHT failed for key: '" + aKey + "' " + ioe.getMessage());
	    return false;
	}
    }
    

    @Override
    public void announce(AbstractAnnounceMessage m) {
        dht.announce(m);
    }
    
    
    @Override
    public String toString() {
        return dht.toString();
    }
    

    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        // this TomP2P IM Node is a listener for announce messages from the DHT
        // it is also a handler of those events as they are passed on to the
        // higher level listener, namely the ControlInformationManager
        
        // notifying the higher level listener
        listener.receivedAnnounceEvent(m);
    }
    
}
