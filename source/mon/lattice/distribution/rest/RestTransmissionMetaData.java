package mon.lattice.distribution.rest;

import mon.lattice.distribution.MetaData;
import java.net.InetAddress;
import java.io.Serializable;

/**
 * Information about a REST request.
 * Includes: content length, src ip address, dst ip address
 */
public class RestTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final InetAddress srcIPAddr;
    public final InetAddress dstIPAddr;
    public int srcPort = -1;
    
    /**
     * Construct a RestTransmissionMetaData object.
     */
    public RestTransmissionMetaData(int l, InetAddress sia, InetAddress dia) {
	length = l;
	srcIPAddr = sia;
	dstIPAddr = dia;
    }
    
    public RestTransmissionMetaData(int l, InetAddress sia, InetAddress dia, int port) {
        this(l, sia, dia);
        srcPort = port;
    }

    /**
     * RestTransmissionMetaData to string.
     */
    public String toString() {
	return dstIPAddr + ": "  + srcIPAddr + ":" + srcPort + " => " + length;
    }
}
