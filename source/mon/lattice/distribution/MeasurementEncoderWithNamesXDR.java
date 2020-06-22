// MeasurementEncoderWithNames.java
// Author: Fabrizio Pastore
// Date: Feb 2010

package mon.lattice.distribution;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import mon.lattice.core.Measurement;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;

public class MeasurementEncoderWithNamesXDR extends MeasurementEncoderXDR {


    /**
     * Encode the Measurement to a DataOutput object.
     * The message is encoded and it's structure is:
     * <pre>
     * +------------------------------------------------------------------------+
     * | seq no (long) | options (byte) [bit 0 : 0 = no names / 1 = with names] |
     * +------------------------------------------------------------------------+
     * |  probe id (2 X long) | type (utf string)  | timestamp (long)           |
     * +------------------------------------------------------------------------+
     * | time delta (long) | service id (2 X long) | group id (2 X long)        |
     * +------------------------------------------------------------------------+
     * | probe name (utf string) | attr count                                   |
     * +------------------------------------------------------------------------+
     * | attr0 field no (int) | attr0 name (utf string) | attr0 type (byte)     |
     * | attr0 value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * | ....                                                                   |
     * +------------------------------------------------------------------------+
     * | attrN field no (int) | attrN name (utf string) | attrN type (byte)     | 
     * | attrN value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * </pre>
     */
    public void encode(DataOutput out) throws IOException, TypeException {
        this.out = out;

        /* write measurement */

        // write seq no
        out.writeLong(measurement.getSequenceNo());
        // write options byte
        // set bit 0 to 1 to indicate that this has names
        out.writeByte((byte)0x01);
        // write probe id
        ID pid = measurement.getProbeID();
	out.writeLong(pid.getMostSignificantBits());
	out.writeLong(pid.getLeastSignificantBits());
        // write measurement type
        out.writeUTF(measurement.getType());
        // write timestamp
        out.writeLong(measurement.getTimestamp().value());
        // write measurement delta
        out.writeLong(measurement.getDeltaTime().value());
        // write out the service ID
        ID sid = measurement.getServiceID();
	out.writeLong(sid.getMostSignificantBits());
	out.writeLong(sid.getLeastSignificantBits());
        // write out the group ID
        ID gid = measurement.getGroupID();
	out.writeLong(gid.getMostSignificantBits());
	out.writeLong(gid.getLeastSignificantBits());
		
        // added by TOF
        // encode probe name
        out.writeUTF(getProbeName());

        // write attributes
		
        // count first
        int attrCount = measurement.getValues().size();
        out.writeInt(attrCount);

        //System.err.print(" [" + attrCount + "] ");
		
		
        Probe probe = ((ProducerMeasurement)measurement).getProbe();
		
        // skip through all the attributes
		
		
        for (ProbeValue attr : measurement.getValues()) {			  
            // write attr
            int field = attr.getField();		    
		    
            // first write out the attribute field as an integer
            out.writeInt(field);
		    
            // write the attribute name
            String name = getAttributeName(attr, field);
		    
            // then write the type
            Object value = attr.getValue();
            ProbeAttributeType type = ProbeAttributeType.lookup(value);

            //System.err.println("Encoding " + type + " name:" + name + ";" );
		    
            encodeValue(name, ProbeAttributeType.STRING);
		    
            encodeType(type);

            // System.err.print((char)type.getCode());
            // System.err.print(", ");
		    
            // and now the value
            encodeValue(value, type);
		    
		    
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
     * Construct a MeasurementEncoderWithNames for a Measurement.
     */
    public MeasurementEncoderWithNamesXDR(Measurement m) {
        super(m);
    }
	
	

}
