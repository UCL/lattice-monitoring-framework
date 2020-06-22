// DataPlaneMessageXDREncoder.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution;

import mon.lattice.xdr.XDRDataOutputStream;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.MeasurementMessage;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.DataOutput;
import java.io.IOException;


/**
 * Encode a DataPlaneMessage into XDR
 */
public class DataPlaneMessageXDREncoder {
    ByteArrayOutputStream byteStream;
    
    /**
     * Construct a DataPlaneMessageXDREncoder with a ByteArrayOutputStream
     */
    public DataPlaneMessageXDREncoder(ByteArrayOutputStream os) {
        byteStream = os;
    }

    /**
     * Encode a DataPlaneMessage into the ByteArrayOutputStream.
     * By default, encodes Measurements with Names
     */
    public void encode(DataPlaneMessage dsp) throws TypeException, IOException {
        encode(dsp, true);
    }
            
    /**
     * Encode a DataPlaneMessage into the ByteArrayOutputStream.
     * @param dsp a DataPlaneMessage
     * @param withNames is the Measurement encoded with names, true is yes, false is no
     */
    public void encode(DataPlaneMessage dsp, boolean withNames) throws TypeException, IOException {
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
            MeasurementEncoderXDR encoder = null;

            if (withNames) {
                encoder = new MeasurementEncoderWithNamesXDR(measurement);
            } else {
                encoder = new MeasurementEncoderXDR(measurement);
            }
            
            // encode a measurement as XDR onto the dataOutput
            encoder.encode(dataOutput);

            break;
        }

    }
}
