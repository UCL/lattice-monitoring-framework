// WSTransmissionMetaData.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.MetaData;
import java.net.InetAddress;
import java.io.Serializable;

/**
 * Information about a transmission.
 * Includes: packet length, src ip address, dst ip address
 */
public class WSTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final InetAddress srcIPAddr;
    public final InetAddress dstIPAddr;
    public int srcPort = -1;
    
    /**
     * Construct a WSTransmissionMetaData object.
     */
    public WSTransmissionMetaData(int l, InetAddress sia, InetAddress dia) {
	length = l;
	srcIPAddr = sia;
	dstIPAddr = dia;
    }
    
    public WSTransmissionMetaData(int l, InetAddress sia, InetAddress dia, int port) {
        this(l, sia, dia);
        srcPort = port;
    }

    /**
     * WSTransmissionMetaData to string.
     */
    public String toString() {
	return dstIPAddr + ": "  + srcIPAddr + ":" + srcPort + " => " + length;
    }
}
