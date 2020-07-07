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

public class ZMQDataPlaneConsumerAndForwarder extends AbstractZMQDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a UDPDataPlaneConsumerNoNames.
     */
    
    ZMQDataForwarder forwarder;
    
    public ZMQDataPlaneConsumerAndForwarder(int port) {
        super(port);
    }

    
    @Override
        public boolean connect() {
	try {
	    // only connect if we're not already connected
            if (forwarder == null) {
                    forwarder = new ZMQDataForwarder(port);
                    forwarder.startProxy();
                }
            
	    if (subscriber == null) {
                // connecting to the internal inproc
                subscriber = new ZMQDataSubscriber(this, forwarder.getInternalURI(), forwarder.getContext());
                subscriber.connect();
                subscriber.listen();
		return true;
	    } else {
		return true;
	    }

	} catch (Exception ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}

    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    @Override
    public boolean disconnect() {
	try {
            forwarder.stopProxy();
	    subscriber.end();
	    subscriber = null;
	    return true;
	} catch (Exception ieo) {
	    subscriber = null;
	    return false;
	}
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
