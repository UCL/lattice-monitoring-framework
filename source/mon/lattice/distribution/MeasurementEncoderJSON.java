// MeasurementEncoderJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution;

import mon.lattice.core.Measurement;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeValue;
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
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

/**
 * Convert a measurement to a JSON representation.
 * The measurement is expected to be with names
 */
public class MeasurementEncoderJSON {
    // The Measurement
    Measurement measurement;

    

    /**
     * Construct a MeasurementEncoderJSON for a Measurement.
     */
    public MeasurementEncoderJSON(Measurement m) {
	measurement = m;
    }

    public void encode(JSONObject json) throws JSONException, TypeException {

        // encode the measurement, ready for transmission

        // write seq no
        json.put("probeSeqNo", measurement.getSequenceNo());

        // write options byte
        // set bit 0 to 1 to indicate that this has names
        json.put("hasNames", true);

        // write probe id
        ID pid = measurement.getProbeID();
        json.put("probeID", pid.toString());
            
        // write measurement type
        json.put("measurementClass", measurement.getType());
            
        // write timestamp
        json.put("timestamp", measurement.getTimestamp().value());

        // write measurement delta
        json.put("tDelta", measurement.getDeltaTime().value());

        // write out the service ID
        ID sid = measurement.getServiceID();
        json.put("serviceID", sid.toString());

        // write out the group ID
        ID gid = measurement.getGroupID();
        json.put("groupID", gid.toString());
		
        // encode probe name

        json.put("probeName", getProbeName());

        // write attributes
		
        // count first
        int attrCount = measurement.getValues().size();

        json.put("attrCount", attrCount);

        // skip through all the attributes
        JSONArray attributes = new JSONArray();
        json.put("attributes", attributes);
		
        for (ProbeValue attr : measurement.getValues()) {
            JSONObject jsonAttr = new JSONObject();
                    
            // write attr
            int field = attr.getField();		    
		    
            // first write out the attribute field as an integer
            jsonAttr.put("fieldNo", field);
		    
            // write the attribute name
            String attributeName = getAttributeName(attr, field);
		    
            // then write the type
            Object value = attr.getValue();
            ProbeAttributeType type = ProbeAttributeType.lookup(value);

            //System.err.println("Encoding " + type + " name:" + name + ";" );
            // Attribute name
            jsonAttr.put("name", attributeName);

            // Encode type
            jsonAttr.put("type", type);

            // System.err.print((char)type.getCode());
            // System.err.print(", ");
		    
            // and now the value
            jsonAttr.put("value", encodeValue( value, type));

            attributes.put(jsonAttr);
    

        }
    }

    /**
     * Get the probe name
     */
    protected String getProbeName() {
        return ((ProducerMeasurement)measurement).getProbe().getName();
    }

    /**
     * Get an attribute name
     */
    protected String getAttributeName(ProbeValue attr, int field) {
        ProbeAttribute attribute = ((ProducerMeasurement)measurement).getProbe().getAttribute(field);
        return attribute.getName();
    }

    /**
     * Encode a value of a given type
     */
    protected Object encodeValue(Object value, ProbeAttributeType type) throws JSONException {	
        // we need to determine which type to return
        switch (type) {

        case BOOLEAN:
            return value;

        case BYTE:
            return value;

        case CHAR:
            return value;

        case SHORT:
            return value;

        case INTEGER:
            return value;

        case LONG:
            return value;

        case FLOAT:
            return value;

        case DOUBLE:
            return value;

        case STRING:
            return value;

        case BYTES:
            // byte arrays go as Base64 in JSON
            
            // In Java 8 onwards can look at doing this:
            // return java.util.Base64.getEncoder().encodeToString((byte[])value);
            
            return javax.xml.bind.DatatypeConverter.printBase64Binary((byte[])value);

        case TABLE:
            // write out a table value
            return writeTable(value);
            //return value.toString();

        case MAP:
            // write out a map value
            return writeMap(value);

        case LIST:
            // write out a list value
            return writeList(value);

        default:
            throw new Error("Unknown ProbeAttributeType: " + type);
            // unreachable break;
        }
    }


    /**
     * List as JSON
     */
    protected JSONObject writeList(Object value) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        
	MList list = (MList)value;

	// write out no of elements of list
        int elemCount = list.size();
        json.put("size", elemCount);

	// write out type of the elements
        json.put("type", list.getType());


	// now write out the elements
	for (int e=0; e < elemCount; e++) {
	    MListValue element = list.get(e);

	    // add element value
	    array.put(encodeValue(element.getValue(), element.getType()));
	}

        json.put("list", array);

        return json;
    }

    /**
     * Map as JSON
     */
    protected JSONObject writeMap(Object value) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        MMap map = (MMap)value;


	// write out no of elements in the map
	int count = map.size();
        json.put("size", count);


	// write out type of the keys
        json.put("keyType", map.getKeyType());

	// write out type of the values
        json.put("valueType", map.getValueType());

        // Get all the entries in the Map
        Set<Map.Entry<MMapValue, MMapValue>> entrySet = map.toMap().entrySet();

        // now write out the mappings
        for (Map.Entry<MMapValue, MMapValue> entry : entrySet) {
            MMapValue eKey = entry.getKey();
            MMapValue eValue = entry.getValue();

            JSONObject mapping = new JSONObject();

            mapping.put("key", encodeValue(eKey.getValue(), eKey.getType()));
            mapping.put("value", encodeValue(eValue.getValue(), eValue.getType()));

            array.put(mapping);
        }

        json.put("map", array);

        return json;
        
    }

    /**
     * Write a table to the output.
     */
    protected JSONObject writeTable(Object value) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray jsonHeader = new JSONArray();
        JSONArray jsonRows = new JSONArray();

	Table table = (Table)value;

	// write out no of rows in table
	int rowCount = table.getRowCount();
        json.put("rowCount", rowCount);

	// write out no of columns in each row
	int colCount = table.getColumnCount();
        json.put("colCount", colCount);


	// write out a list of all the types
	TableHeader header = table.getColumnDefinitions();

	// write out the names and types
	for (int col=0; col < colCount; col++) {
	    TableAttribute attribute = header.get(col);

            JSONObject headerPair = new JSONObject();

	    // write name
	    headerPair.put("name", attribute.getName());

	    // write type
	    headerPair.put("type", attribute.getType());

            jsonHeader.put(headerPair);
	}

	// now write out the values
	// visit rows
	for (int row=0; row < rowCount; row++) {
	    TableRow thisRow = table.getRow(row);

            JSONArray jsonRow = new JSONArray();

	    // visit columns
	    for (int col=0; col < colCount; col++) {
		TableValue element = thisRow.get(col);

		// write element value
		// the decoder can determine the type from the header
		jsonRow.put(encodeValue(element.getValue(), element.getType()));
	    }

            jsonRows.put(jsonRow);
	}

        json.put("header", jsonHeader);
        json.put("rows", jsonRows);

        return json;
        
	
    }    
}
