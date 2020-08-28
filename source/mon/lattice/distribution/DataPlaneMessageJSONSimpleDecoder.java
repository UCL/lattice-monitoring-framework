// DataPlaneMessageJSONSimpleDecoder.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: August 2020

package mon.lattice.distribution;

import mon.lattice.core.plane.MessageType;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;



/**
 * Decode a DataPlaneMessage from JSON
 */
public class DataPlaneMessageJSONSimpleDecoder {
    HashMap<ID, Integer> seqNoMap;
    
    // debug
    int lastSize = 1;

    /**
     * Construct a DataPlaneMessageJSONDecoder with a ByteArrayInputStream
     */
    public DataPlaneMessageJSONSimpleDecoder(HashMap<ID, Integer> seqNoMap) {
        this.seqNoMap = seqNoMap;
    }

    /**
     * Decode a DataPlaneMessage into the ByteArrayOutputStream.
     * By default, decodes Measurements with Names
     */
    public Measurement decode(ByteArrayInputStream bis, MetaData metaData) throws TypeException, ParseException {
        return decode(bis, metaData, true);
    }
            
    /**
     * Decode a DataPlaneMessage from the ByteArrayInputStream.
     * @param withNames is the Measurement decoded with names, true is yes, false is no
     */
    public Measurement decode(ByteArrayInputStream bis, MetaData metaData, boolean withNames) throws TypeException, ParseException {
        // {"attrCount":1,"attributes":[{"fieldNo":0,"name":"elapsedTime","type":"FLOAT","value":16.60585}],"dataSourceID":"3911619c-5dad-4c08-94ac-0c0713718e75","dataSourceSeqNo":8,"groupID":"2","hasNames":true,"measurementClass":"Measurement","messageType":"MEASUREMENT","probeID":"f646d57d-1630-43fe-9df9-5df11836eb11","probeName":"MacBook-Pro-2.local.elapsedTime","probeSeqNo":7,"serviceID":"12345","tDelta":2001,"timestamp":1592505174167}
        // convert the ByteArrayInputStream into a JSON object
        int avail = bis.available();
        byte[] bytes = new byte[avail];
        bis.read(bytes, 0, avail);
        String str = new String(bytes);

        JSONObject json = (JSONObject)JSONValue.parseWithException(str);


        // get the DataSource id
        String dataSourceIDStr = (String)json.get("dataSourceID");
        ID dataSourceID = ID.fromString(dataSourceIDStr);

        // check message type
        String type = (String)json.get("messageType");

        MessageType mType = MessageType.valueOf(type);

        // delegate read to right object
        if (mType == null) {
            //System.err.println("type = " + type);
            return null;
        }

        // get seq no
        int seq = ((Long)json.get("dataSourceSeqNo")).intValue();

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

        /*
        if (seqNoMap.size() != lastSize) {
            System.err.println(getClass().getSimpleName() + ":" +
                               " Thread: " + Thread.currentThread().getName() +
                               " MeasurementReceiver: " + measurementReceiver.getClass().getSimpleName() +
                               " added key: " + dataSourceID +
                               " HashMap size: " + seqNoMap.size());
            lastSize = seqNoMap.size();
        }
        */
        
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
            MeasurementDecoderJSONSimple decoder = new MeasurementDecoderJSONSimple();
            Measurement measurement = decoder.decode(json);

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
