// PlanxIMNode.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.im.dht.planx;

import mon.lattice.core.ID;
import java.io.Serializable;
import java.io.IOException;
import java.math.BigInteger;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.im.dht.AbstractDHTIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An PlanxIMNode is responsible for converting  DataSource, ControllableDataConsumer and Probe
 attributes into Hashtable keys and values for the PlanxDistributedHashTable.
 * <p>
 * For example, with a given DataSource you get:
 * <ul>
 * <li> /datasource/datasource-id/attribute = value
 * </ul>
 * and with a given Probe you get:
 * <ul>
 * <li> /probe/probe-id/attribute = value
 * </ul>
 * @deprecated, use eu.fivegex.monitoring.im.dht.tomp2p.IMNode
 **/

public class PlanxIMNode extends AbstractDHTIMNode {
    // The actual DHT
    PlanxDistributedHashTable dht = null;

    // the local port
    int localPort = 0;

    // the remote host
    //String remoteHost;

    // the remote port
    int remotePort = 0;
    
    static Logger LOGGER = LoggerFactory.getLogger(PlanxIMNode.class);

    /**
     * Construct an IMNode, given a local port and a remote host
     * and a remote port.
     */
    public PlanxIMNode(int myPort, String remHost, int remPort) {
	localPort = myPort;
	remoteHost = remHost;
	remotePort = remPort;
    }

    /**
     * Connect to the DHT peers.
     */
    @Override
    public boolean connect() {
	try {
	    // only connect if we don't already have a DHT
	    if (dht == null) {
		dht = new PlanxDistributedHashTable(localPort);
		dht.connect(remoteHost, remotePort);
                
		LOGGER.info("IMNode: connect: " + localPort + " to " + remoteHost + "/" + remotePort);

		return true;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: connect failed: " + ioe);
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
    public boolean containsDataSource(ID dataSourceID, int timeOut) {
        try {
            BigInteger newKey = keyToBigInteger("/datasource/" + dataSourceID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsDataSource failed for DS " + dataSourceID);
            return false;
        }
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut) {
        try {
            BigInteger newKey = keyToBigInteger("/dataconsumer/" + dataConsumerID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsDataConsumer failed for DC " + dataConsumerID);
            return false;
        }
    }

    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeout) {
        try {
            BigInteger newKey = keyToBigInteger("/controlleragent/" + controllerAgentID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsControllerAgent failed for agent " + controllerAgentID);
            return false;
        }
    }

    @Override
    public boolean containsProbe(ID probeID, int timeout) {
        try {
            BigInteger newKey = keyToBigInteger("/probe/" + probeID + "/name");
            return dht.contains(newKey);
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
	    BigInteger newKey = keyToBigInteger(aKey);
	    //System.out.println("PlanxIMNode: put " + aKey + " K(" + newKey + ") => " + aValue);
	    dht.put(newKey, aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: putDHT failed for key: '" + aKey + "' value: '" + aValue + "'");
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
	    BigInteger newKey = keyToBigInteger(aKey);
	    Object aValue = dht.get(newKey);
	    //System.out.println("PlanxIMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return aValue;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: getDHT failed for key: '" + aKey + "'");
	    ioe.printStackTrace();
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
	    BigInteger newKey = keyToBigInteger(aKey);
	    dht.remove(newKey);
	    //System.out.println("PlanxIMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: remDHT failed for key: '" + aKey + "'");
	    return false;
	}
    }

    /**
     * Convert a key like /a/b/c/d into a fixed size big integer.
     */
    private BigInteger keyToBigInteger(String aKey) {
	// hash codes are signed ints
	int i = aKey.hashCode();
	// convert this into an unsigned long
	long l = 0xffffffffL & i;
	// create the BigInteger
	BigInteger result = BigInteger.valueOf(l);

	return result;
    }
    
    
    @Override
    public String toString() {
        return dht.toString();
    }

    @Override
    public void announce(AbstractAnnounceMessage m) {
        throw new UnsupportedOperationException("Sending announce messages is not supported by this DHT implementation");
    }
    
    
    
    
    
}
