// WSDataPlaneProducerWithNames.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.xdr.XDRDataOutputStream;
import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.MeasurementEncoderWithNames;
import mon.lattice.distribution.MeasurementEncoder;
import mon.lattice.core.plane.MeasurementMessage;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetSocketAddress;

/**
 * A WSDataPlaneProducerWithNames is a DataPlane implementation
 * that sends Measurements by WS.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class WSDataPlaneProducerWithNames extends AbstractWSDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    /**
     * Construct a WSDataPlaneProducerWithNames
     */
    public WSDataPlaneProducerWithNames(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Send a message onto the address.
     * The message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public int transmit(DataPlaneMessage dsp) throws Exception { 
	// convert DataPlaneMessage into a ByteArrayOutputStream
	// then transmit it

        //System.out.println("WSDataPlaneProducerWithNames.transmit " + dsp.toString());
        
	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    DataOutput dataOutput = new XDRDataOutputStream(byteStream);

	    // write the DataSource id
            ID dataSourceID = dsp.getDataSource().getID();
	    dataOutput.writeLong(dataSourceID.getMostSignificantBits());
	    dataOutput.writeLong(dataSourceID.getLeastSignificantBits());

	    // write type
	    dataOutput.writeInt(dsp.getType().getValue());

	    //System.err.println("DSP type = " + dsp.getType().getValue());

	    // write seqNo
	    int seqNo = dsp.getSeqNo();
	    dataOutput.writeInt(seqNo);

	    // write object
	    switch (dsp.getType()) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;

	    case MEASUREMENT:
		// extract Measurement from message object
		ProbeMeasurement measurement = ((MeasurementMessage)dsp).getMeasurement();
		// encode the measurement, ready for transmission
		MeasurementEncoder encoder = new MeasurementEncoderWithNames(measurement);
		encoder.encode(dataOutput);

		break;
	    }

	    //System.err.println("DP: " + dsp + " AS " + byteStream);

	    // now tell the multicaster to transmit this byteStream
            
            
            
	    wsTransmitter.transmit(byteStream, seqNo);

	    return 1;
	} catch (TypeException te) {
	    te.printStackTrace(System.err);
	    return 0;
	}
    }


}