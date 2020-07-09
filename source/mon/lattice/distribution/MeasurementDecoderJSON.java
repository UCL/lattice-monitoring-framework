// MeasurementDecoderJSON.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020


package mon.lattice.distribution;

import mon.lattice.core.data.table.TableAttribute;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.TableException;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.DefaultMMap;
import mon.lattice.core.data.list.DefaultMList;
import mon.lattice.core.data.list.MList;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.core.TypeException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;


/**
 * Convert a measurement as a JSON byte array to a Measurement.
 */
public class MeasurementDecoderJSON {
    JSONObject json;

    public static boolean debug = false;

    /**
     *  Decode a Measurement from a JSON object.
     */    
    public Measurement decode(JSONObject json) throws JSONException, TypeException {
        // {"attrCount":1,"attributes":[{"fieldNo":0,"name":"elapsedTime","type":"FLOAT","value":16.60585}],"dataSourceID":"3911619c-5dad-4c08-94ac-0c0713718e75","dataSourceSeqNo":8,"groupID":"2","hasNames":true,"measurementClass":"Measurement","messageType":"MEASUREMENT","probeID":"f646d57d-1630-43fe-9df9-5df11836eb11","probeName":"MacBook-Pro-2.local.elapsedTime","probeSeqNo":7,"serviceID":"12345","tDelta":2001,"timestamp":1592505174167}

        if (debug) System.err.println("MeasurementDecoderJSON: json = " + json);


        this.json = json;
        
        /* read measurement */

        // read seq no
        long seqNo = json.getLong("probeSeqNo");
		
        // options byte
        boolean options = json.getBoolean("hasNames");

        // check the options
        boolean hasNames = options;


        // read probe id
        String probeIDStr = json.getString("probeID");
        ID probeID = ID.fromString(probeIDStr);
	
        //read measurement type
        String mType = json.getString("measurementClass");

        // read timestamp
        long ts = json.getLong("timestamp");

        // read measurement time delta
        long mDelta = json.getLong("tDelta");

        // read the service ID of the probe
        String serviceIDStr = json.getString("serviceID");

        ID serviceID;

        if (serviceIDStr.contains("-")) {
            // looks like a UUID
            serviceID = ID.fromString(serviceIDStr);
        } else {
            // looks like a number
            long serviceIDMSB = 0;
            long serviceIDLSB = json.getLong("serviceID");

            serviceID = new ID(serviceIDMSB, serviceIDLSB);
        }
		
        // read the group ID of the probe
        String groupIDStr = json.getString("groupID");

        ID groupID;
        
        if (groupIDStr.contains("-")) {
            // looks like a UUID
            groupID = ID.fromString(groupIDStr);
        } else {
            // looks like a number
            long groupIDMSB = 0;
            long groupIDLSB = json.getLong("groupID");

            groupID = new ID(groupIDMSB, groupIDLSB);
        }
		

        // decode probe name
        String probeName = null;

        // check if names are sent
        if (hasNames) {
            probeName = json.getString("probeName");
        } else {
            probeName = "";
        }
		
        // System.err.print(probeID + ": " + mType + " @ " + ts + ". ");

        // read attributes
		
        // read count
        int attrCount = json.getInt("attrCount");
        
        List<ProbeValue> attrValues = new ArrayList<ProbeValue>();

        // System.err.print(" [" + attrCount + "] ");

        JSONArray attributes = json.getJSONArray("attributes");

        // skip through all the attributes
        for (int attr=0; attr < attrCount; attr++) {
            // {"fieldNo":0,"name":"elapsedTime","type":"FLOAT","value":16.60585}
            JSONObject jsonAttr = attributes.getJSONObject(attr);
            
            // read attr key
            int key = jsonAttr.getInt("fieldNo");
            
            Object value = null;

            // System.err.print(key);
            // System.err.print(" -> ");

            // get the attribute name
            String name = null;

            // check if names are sent
            if (hasNames) {
                name = jsonAttr.getString("name");
            } else {
                name = "";
            }
		    
            // read on ProbeAttributeType code
            String typeStr = jsonAttr.getString("type");
            ProbeAttributeType type = decodeType(typeStr);

            // System.err.print(type);
            // System.err.print(", ");

            // now get value
            value = decodeValue(type, jsonAttr.get("value"));

            // System.err.print("<");
            // System.err.print(value);
            // System.err.print(">");

            // save this value
            attrValues.add(new ProbeValueWithName(name, key, value));
        }

        // System.err.println();
	return new ConsumerMeasurementWithMetadataAndProbeName(seqNo, probeID, mType, ts, mDelta, serviceID, groupID, attrValues, probeName);
		
    }
	
    /**
     * Decode a type
     */
    protected ProbeAttributeType decodeType(String typeStr) throws JSONException, TypeException {
	return ProbeAttributeType.valueOf(typeStr);
    }

    /**
     * Decode a value
     */
    protected Object decodeValue(ProbeAttributeType type, Object object) throws JSONException, TypeException {
        // {"fieldNo":0,"name":"string","type":"STRING","value":"MacBook-Pro-2.local.MacBook-Pro-2.local.vee.2.test"},{"fieldNo":1,"name":"float","type":"FLOAT","value":5000},{"fieldNo":2,"name":"double","type":"DOUBLE","value":555.5555419921875},{"fieldNo":3,"name":"long","type":"LONG","value":5487598},{"fieldNo":4,"name":"integer","type":"INTEGER","value":4447},{"fieldNo":5,"name":"short","type":"SHORT","value":2223},{"fieldNo":6,"name":"boolean","type":"BOOLEAN","value":false},{"fieldNo":7,"name":"char","type":"CHAR","value":"M"},{"fieldNo":8,"name":"byte","type":"BYTE","value":97},{"fieldNo":9,"name":"bytes","type":"BYTES","value":"TWFjQm9vay1Qcm8tMi5sb2NhbC52ZWUuMi50ZXN0"},{"fieldNo":10,"name":"list","type":"LIST","value":{"list":[1,2,3],"size":3,"type":"INTEGER"}},{"fieldNo":11,"name":"map","type":"MAP","value":{"keyType":"STRING","map":[{"key":"one","value":1},{"key":"two","value":2},{"key":"three","value":3}],"size":3,"valueType":"INTEGER"}},{"fieldNo":12,"name":"table","type":"TABLE","value":{"colCount":3,"header":[{"name":"name","type":"STRING"},{"name":"type","type":"STRING"},{"name":"value","type":"INTEGER"}],"rowCount":2,"rows":[["stuart","person",42],["hello","world",180]]}}

	Object value = null;
	byte typeCode = type.getCode();


        //System.err.println("typeCode = " + (char)typeCode + " object = " + object + " class = " + object.getClass().getName());

	// we need to determine the type to read
	switch (typeCode) {
	case 'Z':
	    value = Boolean.valueOf((boolean)object);
	    break;

	case 'B':
	    value = ((Number)object).byteValue();
	    break;

	case 'C':
	    value = ((String)object).charAt(0);
	    break;

	case 'S':
	    value = ((Number)object).shortValue();
	    break;

	case 'I':
	    value = ((Number)object).intValue();
	    break;

	case 'J':
	    value = ((Number)object).longValue();
	    break;

	case 'F':
	    value = ((Number)object).floatValue();
	    break;

	case 'D':
	    value = ((Number)object).doubleValue();
	    break;

	case '"':
	    value = object;
	    break;

	case ']':
            /*
             * Base64 decoder 
             * static byte[]	parseBase64Binary(String lexicalXSDBase64Binary)
             * Converts the string argument into an array of bytes.
             */
            value = javax.xml.bind.DatatypeConverter.parseBase64Binary((String)object);
	    break;

	case 'T':
	    value = readTable((JSONObject)object);
	    break;

	case 'M':
	    value = readMap((JSONObject)object);
	    break;

	case 'L':
	    value = readList((JSONObject)object);
	    break;

	default:
	    // System.err.print(" ? ");
	    break;
	}

	return value;
    }


    /**
     * Read a table from the input.
     */
    protected Table readTable(JSONObject json) throws JSONException, TypeException {
        System.err.println("readTable json = " + json);

	// allocate a table
	Table table = new DefaultTable();

	// get no of rows
	int rowCount = json.getInt("rowCount");

	// get no of columns
	int colCount = json.getInt("colCount");

	// create table definition
	TableHeader header = new DefaultTableHeader();

        JSONArray headerPairs = json.getJSONArray("header");
        
	// now read colCount column definitions
	for (int col=0; col < colCount; col++) {
            JSONObject headerPair = headerPairs.getJSONObject(col);

	    // read name
	    String name = headerPair.getString("name");

	    // read type
            String typeStr = headerPair.getString("type");
            ProbeAttributeType type = decodeType(typeStr);

	    // add it to the header
	    header.add(name, type);
	    
	}

	// set column definitions
	table.defineTable(header);

        JSONArray rows = json.getJSONArray("rows");
        
	// now read all of the rows
	for (int row=0; row < rowCount; row++) {
            JSONArray jsonRow = rows.getJSONArray(row);

	    TableRow thisRow = new DefaultTableRow();

	    // visit columns
	    for (int col=0; col < colCount; col++) {
		// find the type in the header
		TableAttribute attribute = header.get(col);
		ProbeAttributeType type = attribute.getType();

		// decode a value, 
		Object value = decodeValue(type, jsonRow.get(col));

		// add value to the row
		thisRow.add(value);
	    }

	    // add the row to the table
	    try {
		table.addRow(thisRow);
	    } catch (TableException te) {
		// if the data is bad it must be a transmission error
		throw new JSONException(te.getMessage());
	    }
	}

	return table;

    }

    /**
     * Read a map from the input.
     */
    protected MMap readMap(JSONObject json) throws JSONException, TypeException {
        System.err.println("readMap json = " + json);

	// Read the size
	int mapSize = json.getInt("size");

	// Read the key type
        String keyTypeStr = json.getString("keyType");
	ProbeAttributeType keyType = decodeType(keyTypeStr);

	// Read the value type
        String valueTypeStr = json.getString("valueType");
	ProbeAttributeType valueType = decodeType(valueTypeStr);

        // Allocate a map of the right type
	MMap map = new DefaultMMap(keyType, valueType);

        JSONArray array = json.getJSONArray("map");

	// now add all the values to the map
	for (int e=0; e < mapSize; e++) {
            JSONObject mapping = array.getJSONObject(e);
            
	    // decode a key
	    Object key = decodeValue(keyType, mapping.get("key"));
	    // decode a value, 
	    Object value = decodeValue(valueType, mapping.get("value"));

            map.put(key, value);
	}

	return map;



    }


    /**
     * Read a list from the input.
     */
    protected MList readList(JSONObject json) throws JSONException, TypeException {
        System.err.println("readList json = " + json);

	// Read the size
	int listSize = json.getInt("size");

	// Read the type
        String typeStr = json.getString("type");
        ProbeAttributeType type = decodeType(typeStr);

	// Allocate a list of the right type
	MList list = new DefaultMList(type);

        JSONArray array = json.getJSONArray("list");

	// now add all the values to the list
	for (int e=0; e < listSize; e++) {
	    // decode a value, 
	    Object value = decodeValue(type, array.get(e));

	    list.add(value);
	}

	return list;
    }

    
}
