package mon.lattice.appl.demo.iot;

import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * An AbstractRestDataPlaneConsumer is a DataPlane implementation
 * that receives Measurements via REST.
 * The type of messaging encoding is left for implementation in the subclasses.
 */
public abstract class AbstractRestDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving, Container {
    
    Container container;
    Server server;
    Connection connection;
    SocketAddress address;

    //String address;
    int port;
    
    // the MeasurementReceiver
    MeasurementReceiver measurementReceiver;

    // This keeps the last seqNo from each DataSource that is seen
    HashMap<ID, Integer> seqNoMap;

    
    
    /**
     * Construct a AbstractUDPDataPlaneConsumer.
     */
    public AbstractRestDataPlaneConsumer(int port) throws IOException {
        this.port = port;
	seqNoMap = new HashMap<ID, Integer>();
        container = this;
        server = new ContainerServer(container);
        address = new InetSocketAddress(port);
    }
    
    
    /**
     * Construct a AbstractUDPDataPlaneConsumer connecting to a remote address.
     */
    public AbstractRestDataPlaneConsumer(String host, int port) throws IOException {
        this.port = port;
	seqNoMap = new HashMap<ID, Integer>();
        container = this;
        server = new ContainerServer(container);
        address = new InetSocketAddress(host, port);
    }

    /**
     * Connect to a delivery mechanism.
     */
    public boolean connect() {
	try {
            if (server != null) {
                connection = new SocketConnection(server);
                connection.connect(address);
		return true;
	    } else {
		return false;
	    }

	} catch (IOException ioe) {
	    return false;
	}

    }

    /**
     * Disconnect from a delivery mechanism.
     */
    public boolean disconnect() {
	try {
            connection.close();
	    return true;
	} catch (IOException ieo) {
	    return false;
	}
    }

    /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	return true;
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	return true;
    }


    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException;

    /**
     * This method is called just after there has been EOF
     * in received from some underlying transport.
     */
    public void eof() {
        disconnect();
    }


    /**
     * This method is called just after there has been an error
     * in received from some underlying transport.
     * This passes the exception into the Receiving object.
     */
    public void error(Exception e) {

    }

    /**
     * Send a message.
     */
    public int sendData(DataPlaneMessage dpm) throws Exception {
	return -1;
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id) {
	return false;
    }

    /**
     * Receiver of a measurement, with an extra object that has context info
     */
    public Measurement report(Measurement m) {
	measurementReceiver.report(m);
	return m;
    }

    /**
     * Set the object that will receive the measurements.
     */
    public Object setMeasurementReceiver(MeasurementReceiver mr) {
	Object old = measurementReceiver;
	measurementReceiver = mr;
	return old;
    }


    /**
     * Get the Map that holds last seqNo from each DataSource that has been seen.
     */
    HashMap<ID, Integer> getSeqNoMap() {
        return seqNoMap;
    }

}
