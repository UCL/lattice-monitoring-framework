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
 * A ZMQDataPlaneConsumerAndForwarder receives measurements on the Data Plane and 
 * forwards them using a PUB socket with either bind or connect.
 * 
 * @author uceeftu
 */
public class ZMQDataPlaneConsumerAndForwarder extends AbstractZMQDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {    
    AbstractZMQDataForwarder forwarder;
    String remoteForwardingHost;
    Integer remoteForwardingPort;
    Integer localForwardingPort;
    
    /**
     * Construct a ZMQDataPlaneConsumerAndForwarder that will listen for incoming 
     * measurements on port @port and will forward them via a publisher socket bound on localForwardingPort
     * @param port
     * @param localForwardingPort 
     */
    public ZMQDataPlaneConsumerAndForwarder(int port, int localForwardingPort) {
        super(port);
        this.localForwardingPort = localForwardingPort;
    }

    /**
     * Construct a ZMQDataPlaneConsumerAndForwarder that will listen for incoming 
     * measurements on port @port and will forward them to a publisher socket connected to
     * tcp://remoteForwardingHost:remoteForwardingPort
     * 
     * @param port
     * @param remoteForwardingHost
     * @param remoteForwardingPort 
     */
    public ZMQDataPlaneConsumerAndForwarder(int port, String remoteForwardingHost, int remoteForwardingPort) {
        super(port);
        this.remoteForwardingHost = remoteForwardingHost;
        this.remoteForwardingPort = remoteForwardingPort;
    }
    
    
    @Override
        public boolean connect() {
	try {
	    // only connect if we're not already connected
            if (forwarder == null) {
                // check the parameters to start the right forwarder
                if (remoteForwardingHost != null && remoteForwardingPort != null)
                    forwarder = new ZMQDataForwarderWithConnect(port, remoteForwardingHost, remoteForwardingPort);
                else if (localForwardingPort != null)
                    forwarder = new ZMQDataForwarderWithBind(port, localForwardingPort);
                    
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
     * Disconnect from a delivery mechanism.
     */
    @Override
    public boolean disconnect() {
	try {
            forwarder.stopProxy();
	    subscriber.end();
            forwarder.closeContext();
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
