package mon.lattice.distribution.zmq;

import mon.lattice.distribution.MetaData;
import java.io.Serializable;

/**
 * Information about a ZMQ transmission.
 * Includes: packet length, current HWM and destination Identity
 */
public class ZMQTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final int highWaterMark;
    public String dstIdentity;
    
    /**
     * Construct a ZM!TransmissionMetaData object.
     */
    public ZMQTransmissionMetaData(int l, int hwm, byte[] dstIdent) {
	length = l;
	highWaterMark = hwm;
	dstIdentity = new String(dstIdent);
    }


    /**
     * ZMQTransmissionMetaData to string.
     */
    public String toString() {
	return dstIdentity + " (" + length + ", " + highWaterMark + ")";
    }
}