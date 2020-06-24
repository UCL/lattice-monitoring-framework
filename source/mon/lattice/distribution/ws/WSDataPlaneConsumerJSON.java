// WSDataPlaneConsumerJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.MeasurementDecoderJSON;
import mon.lattice.distribution.ConsumerMeasurementWithMetaData;
import mon.lattice.distribution.MessageMetaData;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.MessageType;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.DataInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

public class WSDataPlaneConsumerJSON extends AbstractWSDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {

    // debug
    int lastSize = 1;


    /**
     * Construct a WSDataPlaneConsumerJSON.
     */
    public WSDataPlaneConsumerJSON(InetSocketAddress addr) {
        super(addr);
    }

    public WSDataPlaneConsumerJSON(int port) {
        super(port);
    }

    /**
     * This method is called just after a message
     * has been received from some underlying transport
     * at a particular address.
     * 
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {

        // {"attrCount":1,"attributes":[{"fieldNo":0,"name":"elapsedTime","type":"FLOAT","value":16.60585}],"dataSourceID":"3911619c-5dad-4c08-94ac-0c0713718e75","dataSourceSeqNo":8,"groupID":"2","hasNames":true,"measurementClass":"Measurement","messageType":"MEASUREMENT","probeID":"f646d57d-1630-43fe-9df9-5df11836eb11","probeName":"MacBook-Pro-2.local.elapsedTime","probeSeqNo":7,"serviceID":"12345","tDelta":2001,"timestamp":1592505174167}
	try {
            // convert the ByteArrayInputStream into a JSON object
            int avail = bis.available();
            byte[] bytes = new byte[avail];
            bis.read(bytes, 0, avail);
            String str = new String(bytes);

            JSONObject json = new JSONObject(str);


	    // get the DataSource id
            String dataSourceIDStr = json.getString("dataSourceID");
	    ID dataSourceID = ID.fromString(dataSourceIDStr);

	    // check message type
	    String type = json.getString("messageType");

	    MessageType mType = MessageType.valueOf(type);

	    // delegate read to right object
	    if (mType == null) {
		//System.err.println("type = " + type);
		return;
	    }

	    // get seq no
	    int seq = json.getInt("dataSourceSeqNo");

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

            if (seqNoMap.size() != lastSize) {
                System.err.println(getClass().getSimpleName() + ":" +
                               " Thread: " + Thread.currentThread().getName() +
                               " MeasurementReceiver: " + measurementReceiver.getClass().getSimpleName() +
                               " added key: " + dataSourceID +
                               " HashMap size: " + seqNoMap.size());
                lastSize = seqNoMap.size();
            }
            
	    //System.err.println("Received " + type + ". mType " + mType + ". seq " + seq);

	    // Message meta data
	    MessageMetaData msgMetaData = new MessageMetaData(dataSourceID, seq, mType);

	    // read object and check it's type
	    switch (mType) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;

	    case MEASUREMENT:
		// decode the bytes into a measurement object
		MeasurementDecoderJSON decoder = new MeasurementDecoderJSON();
		Measurement measurement = decoder.decode(json);

		if (measurement instanceof ConsumerMeasurementWithMetaData) {
		    // add the meta data into the Measurement
		    ((ConsumerMeasurementWithMetaData)measurement).setMessageMetaData(msgMetaData);
		    ((ConsumerMeasurementWithMetaData)measurement).setTransmissionMetaData(metaData);
		}

		
		//System.err.println("DC: datainputstream left = " + dataIn.available());
		// report the measurement
		report(measurement);
		//System.err.println("DC: m = " + measurement);
		break;
	    }


	} catch (JSONException ioe) {
            ioe.printStackTrace();
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw new IOException(ioe.getMessage());
	} catch (Exception e) {
            e.printStackTrace();
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
            throw new TypeException(e.getMessage());
        }
    }

}
