// Based on RandomProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.appl.demo;

public class SensorEmulatorProbe extends RandomProbe {
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor) {
        super(name, fieldName, scaleFactor);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate) {
        super(name, fieldName, scaleFactor, rate);
    }
    
    
}