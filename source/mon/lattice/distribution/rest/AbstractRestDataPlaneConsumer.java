package mon.lattice.distribution.rest;

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
    InetSocketAddress address;
    
    String endPoint;

    //String address;
    int port;
    
    // the MeasurementReceiver
    MeasurementReceiver measurementReceiver;

    // This keeps the last seqNo from each DataSource that is seen
    HashMap<ID, Integer> seqNoMap;

    
    
    /**
     * Construct a AbstractRestDataPlaneConsumer listening on a given port.
     */
    public AbstractRestDataPlaneConsumer(int port, String endP) throws IOException {
        this(null, port, endP, 0);
    }
    
    
    /**
     * Construct a AbstractRestDataPlaneConsumer listening on a given port and a given thread pool size.
     */
    public AbstractRestDataPlaneConsumer(int port, String endP, int threads) throws IOException {
        this(null, port, endP, threads);
    }
    
    
    /**
     * Construct a AbstractRestDataPlaneConsumer listening on a given address and port.
     */
    public AbstractRestDataPlaneConsumer(String host, int port, String endP) throws IOException {
        this(host, port, endP, 0);
    }
    
    
    /**
     * Construct a AbstractRestDataPlaneConsumer listening on a given address and port.
     * It also receives the specific number of working threads in the pool.
     */
    public AbstractRestDataPlaneConsumer(String host, int port, String endP, int threads) throws IOException {
        this.port = port;
        endPoint = endP;
	seqNoMap = new HashMap<ID, Integer>();
        container = this;
        
        if (threads > 0)
            server = new ContainerServer(container, threads);
        else
            // using the default number of threads
            server = new ContainerServer(container);
        
        if (host == null)
            address = new InetSocketAddress(port);
        else {
            address = new InetSocketAddress(host, port);
            if (address.isUnresolved())
                throw new IOException("Cannot solve " + host);
        }
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
