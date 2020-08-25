// UDPDataPlaneConsumerNoNames.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package mon.lattice.distribution.udp;

import mon.lattice.distribution.Receiving;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.DataPlaneMessageXDRDecoder;
import mon.lattice.xdr.XDRDataInputStream;
import mon.lattice.core.plane.MessageType;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class UDPDataPlaneConsumerNoNames extends AbstractUDPDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a UDPDataPlaneConsumerNoNames.
     */
    public UDPDataPlaneConsumerNoNames(InetSocketAddress addr) {
        super(addr);
    }

    public UDPDataPlaneConsumerNoNames(int port) {
        super(port);
    }


    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular address.
     * The expected message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {

	//System.out.println("DC: Received " + metaData);

	try {
            DataPlaneMessageXDRDecoder decoder = new DataPlaneMessageXDRDecoder(getSeqNoMap());
            Measurement measurement = decoder.decode(bis, metaData, false);

            report(measurement);

	} catch (IOException ioe) {
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw ioe;
	} catch (Exception e) {
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
            throw new TypeException(e.getMessage());
	}
    }

}
