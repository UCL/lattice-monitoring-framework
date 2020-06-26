// DataPlaneMessageXDRDecoder.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution;

import mon.lattice.distribution.MessageMetaData;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.MeasurementDecoderXDR;
import mon.lattice.distribution.ConsumerMeasurementWithMetaData;
import mon.lattice.xdr.XDRDataInputStream;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.MeasurementMessage;
import mon.lattice.core.plane.MessageType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.DataInput;
import java.io.IOException;


/**
 * Decode a DataPlaneMessage from XDR
 */
public class DataPlaneMessageXDRDecoder {
    HashMap<ID, Integer> seqNoMap;
    
    /**
     * Construct a DataPlaneMessageXDRDecoder with a ByteArrayInputStream
     */
    public DataPlaneMessageXDRDecoder(HashMap<ID, Integer> seqNoMap) {
        this.seqNoMap = seqNoMap;
    }

    /**
     * Decode a DataPlaneMessage into the ByteArrayOutputStream.
     * By default, decodes Measurements with Names
     */
    public Measurement decode(ByteArrayInputStream bis, MetaData metaData) throws TypeException, IOException {
        return decode(bis, metaData, true);
    }
            
    /**
     * Decode a DataPlaneMessage from the ByteArrayInputStream.
     * @param withNames is the Measurement decoded with names, true is yes, false is no
     */
    public Measurement decode(ByteArrayInputStream bis, MetaData metaData, boolean withNames) throws TypeException, IOException {

        DataInput dataIn = new XDRDataInputStream(bis);

        //System.err.println("DC: datainputstream available = " + dataIn.available());

        // get the DataSource id
        long dataSourceIDMSB = dataIn.readLong();
        long dataSourceIDLSB = dataIn.readLong();
        ID dataSourceID = new ID(dataSourceIDMSB, dataSourceIDLSB);

        // check message type
        int type = dataIn.readInt();

        MessageType mType = MessageType.lookup(type);

        // delegate read to right object
        if (mType == null) {
            //System.err.println("type = " + type);
            return null;
        }

        // get seq no
        int seq = dataIn.readInt();

        /*
         * Check the DataSource seq no.
         */
        if (seqNoMap.containsKey(dataSourceID)) {
            // we've seen this DataSource before
            int prevSeqNo = seqNoMap.get(dataSourceID);

            if (seq == prevSeqNo + 1) {
                // we got the correct message from that DataSource
                // save this seqNo
                seqNoMap.put(dataSourceID, seq);
            } else {
                // a DataSource message is missing
                // TODO: decide what to do
                // currently: save this seqNo
                seqNoMap.put(dataSourceID, seq);
            }
        } else {
            // this is a new DataSource
            seqNoMap.put(dataSourceID, seq);
        }

        //System.err.println("Received " + type + ". mType " + mType + ". seq " + seq);

        // Message meta data
        MessageMetaData msgMetaData = new MessageMetaData(dataSourceID, seq, mType);

        // read object and check it's type
        switch (mType) {

        case ANNOUNCE:
            System.err.println("ANNOUNCE not implemented yet!");
            return null;

        case MEASUREMENT:
            // decode the bytes into a measurement object
            MeasurementDecoderXDR decoder = new MeasurementDecoderXDR();
            Measurement measurement = decoder.decode(dataIn);

            if (measurement instanceof ConsumerMeasurementWithMetaData) {
                // add the meta data into the Measurement
                ((ConsumerMeasurementWithMetaData)measurement).setMessageMetaData(msgMetaData);
                ((ConsumerMeasurementWithMetaData)measurement).setTransmissionMetaData(metaData);
            }

		
            //System.err.println("DC: datainputstream left = " + dataIn.available());
            //System.err.println("DC: m = " + measurement);

            return measurement;
                
        }

        return null;
    }
}
