// ConsumerMeasurementToJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.distribution;

import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.distribution.ConsumerMeasurementWithMetadataAndProbeName;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.core.ID;
import mon.lattice.core.data.table.TableAttribute;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableValue;
import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.MMapValue;
import mon.lattice.core.data.list.MListValue;
import mon.lattice.core.data.list.MList;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

/**
 * Convert a ConsumerMeasurement to a JSON representation.
 */
public class ConsumerMeasurementToJSON extends MeasurementEncoderJSON {
    // This is basically the same as a MeasurementEncoderJSON
    // with a few specific differences to get the Probe name
    // and the attribute names

    /**
     * Construct a MeasurementEncoderJSON for a Measurement.
     */
    public ConsumerMeasurementToJSON(Measurement m) {
        this((ConsumerMeasurement)m);
    }
    
    public ConsumerMeasurementToJSON(ConsumerMeasurement m) {
        super(m);
    }


    /**
     * Get the probe name
     */
    protected String getProbeName() {
        return ((ConsumerMeasurementWithMetadataAndProbeName)measurement).getProbeName();
    }

    /**
     * Get an attribute name
     */
    protected String getAttributeName(ProbeValue attr, int field) {
        return ((ProbeValueWithName)attr).getName();
    }

}
