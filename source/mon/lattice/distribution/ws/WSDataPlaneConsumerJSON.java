// WSDataPlaneConsumerJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.DataPlaneMessageJSONDecoder;
import mon.lattice.distribution.ConsumerMeasurementWithMetaData;
import mon.lattice.distribution.MessageMetaData;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.MessageType;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import us.monoid.json.JSONException;


public class WSDataPlaneConsumerJSON extends AbstractWSDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {

    /**
     * Construct a WSDataPlaneConsumerJSON.
     */
    public WSDataPlaneConsumerJSON(InetSocketAddress addr) {
        super(addr);
    }

    public WSDataPlaneConsumerJSON(int port) {
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
            DataPlaneMessageJSONDecoder decoder = new DataPlaneMessageJSONDecoder(getSeqNoMap());
            Measurement measurement = decoder.decode(bis, metaData);

            report(measurement);


	} catch (JSONException ioe) {
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
