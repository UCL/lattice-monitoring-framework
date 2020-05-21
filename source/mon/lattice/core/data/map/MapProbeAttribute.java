// MapProbeAttribute.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.core.data.map;

import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.AbstractProbeAttribute;

/**
 * An implementation for having a map as a ProbeAttribute
 * These are a Probe's Data Dictionary.
 */
public class MapProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    // The type of the keys
    ProbeAttributeType keyType;
    // The type of the values
    ProbeAttributeType valueType;

    /**
     * Construct a ProbeAttribute.
     */
    public MapProbeAttribute(int field, String name, ProbeAttributeType keyType, ProbeAttributeType valueType) {
	super(field, name, ProbeAttributeType.MAP, "_MAP_");
	this.keyType = keyType;
        this.valueType = valueType;
    }

    /**
     * Get the Key type
     */
    public ProbeAttributeType getKeyType() {
        return keyType;
    }

    /**
     * Get the Value type
     */
    public ProbeAttributeType getValueType() {
        return valueType;
    }

}
