package mon.lattice.appl.demo.iot;

import mon.lattice.appl.demo.RandomProbe;

public class SensorEmulatorProbe extends RandomProbe {
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor) {
        super(name, fieldName, scaleFactor);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate) {
        super(name, fieldName, scaleFactor, rate);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate, String units) {
        super(name, fieldName, scaleFactor, rate, units);
    }
    
    
}