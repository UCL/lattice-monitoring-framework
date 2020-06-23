// ConsumerMeasurementWithMetaDataToJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution;

import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.plane.MessageType;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONException;
import mon.lattice.core.TypeException;

/**
 * Convert a ConsumerMeasurement that has some Message Meta Data to a JSON representation.
 * This is similar to ConsumerMeasurementToJSON, but it also used the Meta Data 
 * to add on the Data Source ID, the Data Source seqNo, and the Message type.
 *
 * This extra data is useful in some contexts.
 */
public class ConsumerMeasurementWithMetaDataToJSON {
    // The Measurement
    ConsumerMeasurement measurement;

    

    /**
     * Construct a ConsumerMeasurementWithMetaDataToJSON for a Measurement.
     */
    public ConsumerMeasurementWithMetaDataToJSON(ConsumerMeasurement m) {
	measurement = m;
    }

    public ConsumerMeasurementWithMetaDataToJSON(Measurement m) {
	measurement = (ConsumerMeasurement)m;
    }

    public void encode(JSONObject json) throws JSONException, TypeException {

        // encode the measurement, ready for transmission
        // try and output some data source info
        // this is kept in the MetaData
        ConsumerMeasurementWithMetaData cm = (ConsumerMeasurementWithMetaData)measurement;

        MessageMetaData metaData =  (MessageMetaData)cm.getMessageMetaData();

        // a bit of knowledge of exposed fields
        ID dataSourceID = metaData.dataSourceID;
        int dataSourceSeqNo = metaData.seqNo;
        MessageType type = metaData.type;

        // write the DataSource id
        json.put("dataSourceID", dataSourceID.toString());

        // write type
        json.put("messageType", type);

        // write seqNo
        json.put("dataSourceSeqNo", dataSourceSeqNo);
            

        // encode the measurement as JSON, ready for transmission
        ConsumerMeasurementToJSON encoder = new ConsumerMeasurementToJSON(cm);

        // encode into an existing JSONObject
        encoder.encode(json);
    }

}
