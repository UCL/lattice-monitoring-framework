package mon.lattice.im.dht;

import java.io.Serializable;


public interface DHTInteracter {

    /**
     * Lookup info directly from the DHT.
     * @return the value if found, null otherwise
     */
    Object getDHT(String aKey);

    /**
     * Put stuff into DHT.
     */
    boolean putDHT(String aKey, Serializable aValue);

    /**
     * Remove info from the DHT.
     * @return boolean
     */
    boolean remDHT(String aKey);
    
}
