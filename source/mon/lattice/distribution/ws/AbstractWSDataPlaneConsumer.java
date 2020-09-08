// AbstractWSDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

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
import java.util.HashMap;
import java.net.InetSocketAddress;

public abstract class AbstractWSDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    // The address we are sending to
    InetSocketAddress address;

    int port;
    
    int nThreads;
    
    // We don't want to transmit measurement data.
    // Producers will only transmit, and Consumers will receive.

    // The WSReceiver
    WSReceiver wsReceiver;

    // the MeasurementReceiver
    MeasurementReceiver measurementReceiver;

    // This keeps the last seqNo from each DataSource that is seen
    HashMap<ID, Integer> seqNoMap;

    
    
    /**
     * Construct a AbstractWSDataPlaneConsumer.
     */
    public AbstractWSDataPlaneConsumer(int port) {
	// sending address is explicitly set to null
	address = null;
        
        this.port = port;

	seqNoMap = new HashMap<ID, Integer>();
    }
    
    /**
     * Construct a AbstractWSDataPlaneConsumer.
     */
    public AbstractWSDataPlaneConsumer(InetSocketAddress addr) {
	// sending address
	address = addr;

	seqNoMap = new HashMap<ID, Integer>();
    }
    
    
    public AbstractWSDataPlaneConsumer(int port, int nThreads) {
        this(port);
        this.nThreads = nThreads;
    }
    
    /**
     * Construct a AbstractWSDataPlaneConsumer.
     */
    public AbstractWSDataPlaneConsumer(InetSocketAddress addr, int nThreads) {
	this(addr);
        this.nThreads = nThreads;
    }
    
    

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (wsReceiver == null) {
                
                WSReceiver rr;
                if (address != null)
                    rr = new WSReceiver(this, address);
                else
                    rr = new WSReceiver(this, port);

		rr.listen();
		
		wsReceiver = rr;

		return true;
	    } else {
		return true;
	    }

	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}

    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	try {
	    wsReceiver.end();
	    wsReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    wsReceiver = null;
	    return false;
	}
    }

    /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	// do nothing currenty
	return true;
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	// do nothing currenty
	return true;
    }


    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular address.
     * The expected message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
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
	//System.err.println("DataConsumer: notified of error " + e.getMessage());
        /*
	System.err.println("Stack Trace:");
	e.printStackTrace(System.err);
        */
    }

    /**
     * Send a message.
     */
    public int sendData(DataPlaneMessage dpm) throws Exception {
	// currenty do nothing
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
	//System.out.println("FT: AbstractWSDataPlaneConsumer.report - UDPDataPlaneConsumer: got " + m);
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
