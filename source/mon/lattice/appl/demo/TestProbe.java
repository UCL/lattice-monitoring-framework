// TestProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2010

package mon.lattice.appl.demo;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.data.list.MList;
import mon.lattice.core.data.list.DefaultMList;
import mon.lattice.core.data.list.ListProbeAttribute;
import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.DefaultMMap;
import mon.lattice.core.data.map.MapProbeAttribute;
import mon.lattice.core.data.table.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * A probe for testing different data types in a measurement.
 */
public class TestProbe extends AbstractProbe implements Probe  {
    Random randomNo;

    // the vee name == FQN
    String vee;

    /*
     * Construct a probe
     */
    public TestProbe(String vee, String currentHost) {
	this.vee = vee;

        setName(currentHost + "." + vee);
        setDataRate(new EveryNSeconds(5));  // every 5 seconds

	randomNo = new Random();


	// data to be sent tests the built-in data types

        addProbeAttribute(new DefaultProbeAttribute(0, "string", ProbeAttributeType.STRING, "STRING"));
        addProbeAttribute(new DefaultProbeAttribute(1, "float", ProbeAttributeType.FLOAT, "FLOAT"));
        addProbeAttribute(new DefaultProbeAttribute(2, "double", ProbeAttributeType.DOUBLE, "DOUBLE"));
        addProbeAttribute(new DefaultProbeAttribute(3, "long", ProbeAttributeType.LONG, "LONG"));
        addProbeAttribute(new DefaultProbeAttribute(4, "integer", ProbeAttributeType.INTEGER, "INTEGER"));
        addProbeAttribute(new DefaultProbeAttribute(5, "short", ProbeAttributeType.SHORT, "SHORT"));
        addProbeAttribute(new DefaultProbeAttribute(6, "boolean", ProbeAttributeType.BOOLEAN, "BOOLEAN"));
        addProbeAttribute(new DefaultProbeAttribute(7, "char", ProbeAttributeType.CHAR, "CHAR"));
        addProbeAttribute(new DefaultProbeAttribute(8, "byte", ProbeAttributeType.BYTE, "BYTE"));
        addProbeAttribute(new DefaultProbeAttribute(9, "bytes", ProbeAttributeType.BYTES, "BYTES"));
        // List of Integer
        addProbeAttribute(new ListProbeAttribute(10, "list", ProbeAttributeType.INTEGER));
        // Map of String -> Integer
        addProbeAttribute(new MapProbeAttribute(11, "map", ProbeAttributeType.STRING, ProbeAttributeType.INTEGER));

        // A Table

        // First define the Table header
        TableHeader tableHeader = new DefaultTableHeader().
            add("name", ProbeAttributeType.STRING).
            add("type", ProbeAttributeType.STRING).
            add("value", ProbeAttributeType.INTEGER);

        addProbeAttribute(new TableProbeAttribute(12, "table",  tableHeader));
    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
    }
 
    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {	    

	    // determine the scale factor for normalizing cpu per cent
	    // it depends on the no of milliseconds between each reading
	    float scaleFactor = (float)rationalToMillis(getDataRate());

	    // work out vals
            int rand = randomNo.nextInt(16000);
            
            String str = getName();
            float f = scaleFactor;
            double d = scaleFactor / 9;
            long l = rand * 1234;
            int i = rand;
            short s = (short)(rand / 2);
            boolean z = rand % 2 == 0;
            char ch = vee.charAt(0);
            byte by = (byte)vee.charAt(1);
            byte[] arr = vee.getBytes();

            // allocate a list
            MList mList = new DefaultMList(ProbeAttributeType.INTEGER);

            mList.add(1).add(2).add(3);

            // allocate a map
            MMap mMap = new DefaultMMap(ProbeAttributeType.STRING, ProbeAttributeType.INTEGER);

            mMap.put("one", 1);
            mMap.put("two", 2).put("three", 3);

            // allocate a table
            Table table1 = new DefaultTable();
            // get header from attribute
            TableHeader header = ((TableProbeAttribute)getAttribute(12)).getDefinition();
            // define table by header
            table1.defineTable(header);

            try {

                // add a row of values
                TableRow r0 = new DefaultTableRow().
                    add(new DefaultTableValue("stuart")).
                    add(new DefaultTableValue("person")).
                    add(new DefaultTableValue(42));
                
                table1.addRow(r0);

                table1.addRow(new DefaultTableRow().add("hello").add("world").add(180));
            } catch (TableException te) {
            }


            // allocate a list for ProbeValues
	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(13);

	    list.add(new DefaultProbeValue(0, str));
	    list.add(new DefaultProbeValue(1, f));
	    list.add(new DefaultProbeValue(2, d));
	    list.add(new DefaultProbeValue(3, l));
	    list.add(new DefaultProbeValue(4, i));
	    list.add(new DefaultProbeValue(5, s));
	    list.add(new DefaultProbeValue(6, z));
	    list.add(new DefaultProbeValue(7, ch));
	    list.add(new DefaultProbeValue(8, by));
	    list.add(new DefaultProbeValue(9, arr));
	    list.add(new DefaultProbeValue(10, mList));
	    list.add(new DefaultProbeValue(11, mMap));
	    list.add(new DefaultProbeValue(12, table1));

	    // create the Measurement
	    ProbeMeasurement m = new ProducerMeasurement(this, list);

	    // return the measurement
	    return m;

	} catch (TypeException te) {
	    System.err.println("TypeException " + te + " in TestProbe");
	    return null;
	}
    }

}
