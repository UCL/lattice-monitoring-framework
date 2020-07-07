package mon.lattice.distribution.zmq;

import mon.lattice.distribution.Receiving;
import mon.lattice.distribution.MetaData;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import mon.lattice.distribution.DataPlaneMessageJSONDecoder;
import us.monoid.json.JSONException;

/**
 * A ZMQDataPlaneConsumerJSON is a DataPlane implementation
 * that receives JSON encoded Measurements via ZMQ.
 * The probe name and attributes name are received with the measurement
 */
public class ZMQDataPlaneConsumerJSON extends AbstractZMQDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a ZMQDataPlaneConsumerWithNames.
     */
    public ZMQDataPlaneConsumerJSON(int port) {
        super(port);
    }

    public ZMQDataPlaneConsumerJSON(String remoteHost, int port) {
        super(remoteHost, port);
    }

    /**
     * This method is called just after a message
     * has been received from some underlying transport
     * at a particular address.
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {

	//System.out.println("DC: Received " + metaData);

	try {
            DataPlaneMessageJSONDecoder decoder = new DataPlaneMessageJSONDecoder(getSeqNoMap());
            Measurement measurement = decoder.decode(bis, metaData);

            report(measurement);

	} catch (JSONException ioe) {
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw new IOException(ioe.getMessage());
	} catch (Exception e) {
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
            throw new TypeException(e.getMessage());
	}
    }

}
