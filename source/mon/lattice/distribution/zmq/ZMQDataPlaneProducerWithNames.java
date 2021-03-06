package mon.lattice.distribution.zmq;

import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.DataPlaneMessageXDREncoder;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.TypeException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

/**
 * A ZMQDataPlaneProducerWithNames is a DataPlane implementation
 * that sends XDR encoded Measurements via ZMQ.
 * The probe name and attributes name are sent within the measurement.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class ZMQDataPlaneProducerWithNames extends AbstractZMQDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    /**
     * Construct a ZMQDataPlaneProducer
     */
    public ZMQDataPlaneProducerWithNames(String remoteHost, int remotePort) {
        super(remoteHost, remotePort);
    }
    
    public ZMQDataPlaneProducerWithNames(InetSocketAddress inetSockAddr) {
        super(inetSockAddr.getAddress().getHostAddress(), inetSockAddr.getPort());
    }
    

    /**
     * Send a message onto the address.
     * The message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public int transmit(DataPlaneMessage dsp) throws Exception { 
	// convert DataPlaneMessage into a ByteArrayOutputStream
	// then transmit it

	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            DataPlaneMessageXDREncoder encoder = new DataPlaneMessageXDREncoder(byteStream);
            encoder.encode(dsp);
	    //System.err.println("DP: " + dsp + " AS " + byteStream);

	    // now tell the publisher to transmit this byteStream
            int seqNo = dsp.getSeqNo();
            
            int transmitted = publisher.transmit(byteStream, seqNo);
	    return transmitted;
	} catch (TypeException te) {
	    te.printStackTrace(System.err);
	    return 0;
	}
    }


}
