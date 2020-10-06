// RandomProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.appl.demo;

import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.ArrayList;
import java.util.Random;
import mon.lattice.core.datarate.EveryNMilliseconds;

/**
 * A probe that returns a random float.
 */
public class RandomProbe extends AbstractProbe implements Probe  {
    Random randomNo;
    int scaleFactor;
    
    // by default there is 1 attribute
    int nAttr = 1;

    /*
     * Construct a probe
     */
    public RandomProbe(String name, String fieldName, int scaleFactor) {
        setName(name);
        setDataRate(new EveryNSeconds(2));
        addProbeAttribute(new DefaultProbeAttribute(0, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    public RandomProbe(String name, String fieldName, Integer scaleFactor) {
        setName(name);
        setDataRate(new EveryNSeconds(2));
        addProbeAttribute(new DefaultProbeAttribute(0, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    public RandomProbe(String name, String fieldName, Integer scaleFactor, String units) {
        setName(name);
        setDataRate(new EveryNSeconds(2));
        addProbeAttribute(new DefaultProbeAttribute(0, fieldName, ProbeAttributeType.FLOAT, units));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    public RandomProbe(String name, String fieldName, int scaleFactor, String sID) {
        super(sID);
        setName(name);
        setDataRate(new EveryNSeconds(2));
        addProbeAttribute(new DefaultProbeAttribute(0, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    public RandomProbe(String name, String fieldName, Integer scaleFactor, String units, int nAttr) {
        setName(name);
        setDataRate(new EveryNSeconds(2));
        this.nAttr = nAttr;
        
        for (int i=0; i<nAttr; i++) {
            addProbeAttribute(new DefaultProbeAttribute(i, fieldName + "-" + i, ProbeAttributeType.FLOAT, units));
        }
        
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    

    public RandomProbe(String name, String fieldName, String scaleFactor) {
        this(name, fieldName, Integer.valueOf(scaleFactor));
    }
    
    
    public RandomProbe(String name, String fieldName, String scaleFactor, String rate) {
        this(name, fieldName, Integer.valueOf(scaleFactor));
        this.setDataRate(new EveryNMilliseconds(Integer.valueOf(rate)));
    }
    
    
    public RandomProbe(String name, String fieldName, String scaleFactor, String rate, String units) {
        this(name, fieldName, Integer.valueOf(scaleFactor), units);
        this.setDataRate(new EveryNMilliseconds(Integer.valueOf(rate)));
    }
    
    
    public RandomProbe(String name, String fieldName, String scaleFactor, String rate, String units, String nAttr) {
        this(name, fieldName, Integer.valueOf(scaleFactor), units, Integer.valueOf(nAttr));
        this.setDataRate(new EveryNMilliseconds(Integer.valueOf(rate)));
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(nAttr);
            
            for (int i=0; i<nAttr; i++) {
                float next = scaleFactor + (randomNo.nextFloat() * (scaleFactor / 5));
                list.add(new DefaultProbeValue(i, next));
            }

	    return new ProducerMeasurement(this, list);
	} catch (Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    return null;
	}
    }

}
