package mon.lattice.distribution.zmq;

import mon.lattice.distribution.Receiving;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.DataPlaneMessageXDRDecoder;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A ZMQDataPlaneConsumerWithNames is a DataPlane implementation
 * that receives XDR encoded Measurements via ZMQ.
 * The probe name and attributes name are received with the measurement
 */
public class ZMQDataPlaneConsumerWithNames extends AbstractZMQDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a ZMQDataPlaneConsumerWithNames.
     * It will listen on *: port
     * 
     * @param port 
     */
    public ZMQDataPlaneConsumerWithNames(int port) {
        super(port);
    }

    /**
     * Construct a ZMQDataPlaneConsumerWithNames.
     * It will listen connect to a remote host and remote port
     * 
     * @param remoteHost
     * @param port 
     */
    public ZMQDataPlaneConsumerWithNames(String remoteHost, int port) {
        super(remoteHost, port);
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
            Measurement measurement = decoder.decode(bis, metaData, true);

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
