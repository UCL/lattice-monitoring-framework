// DataPlaneMessageJSONEncoder.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution;

import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.MeasurementMessage;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;


/**
 * Encode a DataPlaneMessage into JSON
 */
public class DataPlaneMessageJSONEncoder {
    ByteArrayOutputStream byteStream;
    
    /**
     * Construct a DataPlaneMessageJSONEncoder with a ByteArrayOutputStream
     */
    public DataPlaneMessageJSONEncoder(ByteArrayOutputStream os) {
        byteStream = os;
    }

    /**
     * Encode a DataPlaneMessage into the ByteArrayOutputStream.
     */
    public void encode(DataPlaneMessage dsp) throws TypeException, IOException {
        JSONObject json = new JSONObject();

        try {


            // write the DataSource id
            ID dataSourceID = dsp.getDataSource().getID();

            json.put("dataSourceID", dataSourceID.toString());

            // write type
            json.put("messageType", dsp.getType());

            //System.err.println("DSP type = " + dsp.getType().getValue());

            // write seqNo
            int seqNo = dsp.getSeqNo();
            json.put("dataSourceSeqNo", seqNo);

            // write object
            switch (dsp.getType()) {

            case ANNOUNCE:
                System.err.println("ANNOUNCE not implemented yet!");
                break;

            case MEASUREMENT:
                // extract Measurement from message object
                ProbeMeasurement measurement = ((MeasurementMessage)dsp).getMeasurement();
                // encode the measurement as JSON, ready for transmission
                MeasurementEncoderJSON encoder = new MeasurementEncoderJSON(measurement);

                // encode into an existing JSONObject
                encoder.encode(json);
		    
                break;
            }

        } catch (JSONException je) {
        }

        // now put the JSONObject into the OutputStream
        String jsonString = json.toString();

        System.err.println("json = " + jsonString);
        
        byteStream.write(jsonString.getBytes());

    }


    
}
