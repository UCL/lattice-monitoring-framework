// WSDataPlaneProducerWithNames.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.DataPlaneMessageXDREncoder;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.TypeException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

/**
 * A WSDataPlaneProducerWithNames is a DataPlane implementation
 * that sends Measurements by WS.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class WSDataPlaneProducerWithNames extends AbstractWSDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    /**
     * Construct a WSDataPlaneProducerWithNames
     */
    public WSDataPlaneProducerWithNames(InetSocketAddress addr) {
        super(addr);
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

        //System.out.println("WSDataPlaneProducerWithNames.transmit " + dsp.toString());
        
	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            DataPlaneMessageXDREncoder encoder = new DataPlaneMessageXDREncoder(byteStream);
            encoder.encode(dsp);

	    //System.err.println("DP: " + dsp + " AS " + byteStream);

	    // now tell the multicaster to transmit this byteStream
            
            int seqNo = dsp.getSeqNo();
            
	    wsTransmitter.transmit(byteStream, seqNo);

	    return 1;
	} catch (TypeException te) {
	    te.printStackTrace(System.err);
	    return 0;
	}
    }


}
