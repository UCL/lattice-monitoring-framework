// WSDataPlaneProducerJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.DataPlaneMessageJSONEncoder;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.TypeException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

/**
 * A WSDataPlaneProducerJSON is a DataPlane implementation
 * that sends Measurements by WebSocket using JSON encoded messages.
 */
public class WSDataPlaneProducerJSON extends AbstractWSDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    /**
     * Construct a WSDataPlaneProducerJSON
     */
    public WSDataPlaneProducerJSON(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Send a message onto the address.
     * The message is JSON encoded and it's structure is:
     */
    public int transmit(DataPlaneMessage dsp) throws Exception { 
	// convert DataPlaneMessage into a ByteArrayOutputStream
	// then transmit it

        //System.out.println("WSDataPlaneProducerJSON.transmit " + dsp.toString());
        
	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            DataPlaneMessageJSONEncoder encoder = new DataPlaneMessageJSONEncoder(byteStream);
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
