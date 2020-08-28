// WSDataPlaneConsumerJSONSimple.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: August 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.DataPlaneMessageJSONSimpleDecoder;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.json.simple.parser.ParseException;


public class WSDataPlaneConsumerJSONSimple extends AbstractWSDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {

    /**
     * Construct a WSDataPlaneConsumerJSONSimple.
     */
    public WSDataPlaneConsumerJSONSimple(InetSocketAddress addr) {
        super(addr);
    }

    public WSDataPlaneConsumerJSONSimple(int port) {
        super(port);
    }

    /**
     * This method is called just after a message
     * has been received from some underlying transport
     * at a particular address.
     * 
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {

	try {
            DataPlaneMessageJSONSimpleDecoder decoder = new DataPlaneMessageJSONSimpleDecoder(getSeqNoMap());
            Measurement measurement = decoder.decode(bis, metaData);

            report(measurement);


	} catch (ParseException ioe) {
            ioe.printStackTrace();
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw new IOException(ioe.getMessage());
	} catch (Exception e) {
            e.printStackTrace();
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
            throw new TypeException(e.getMessage());
        }
    }

}
