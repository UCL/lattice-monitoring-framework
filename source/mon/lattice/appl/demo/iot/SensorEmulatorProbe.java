package mon.lattice.appl.demo.iot;

import java.util.concurrent.ThreadLocalRandom;
import mon.lattice.appl.demo.RandomProbe;
import org.slf4j.LoggerFactory;

public class SensorEmulatorProbe extends RandomProbe {
    
    /**
     * the min amount of msec the probe can wait for before starting to collect
     * measurements
     */
    int waitMin = 0;
    
    /**
    * the max amount of msec the probe can wait for before starting to collect
    * measurements
    */
    int waitMax = 0;
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor) {
        super(name, fieldName, scaleFactor);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate) {
        super(name, fieldName, scaleFactor, rate);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate, String units) {
        super(name, fieldName, scaleFactor, rate, units);
    }
    
    public SensorEmulatorProbe(String name, String fieldName, String scaleFactor, String rate, String units, String waitMin, String waitMax) {
        super(name, fieldName, scaleFactor, rate, units);
        this.waitMin = Integer.valueOf(waitMin);
        this.waitMax = Integer.valueOf(waitMax);
    }

    
    @Override
    public void beginThreadBody() {
        try {
            int randomWait = ThreadLocalRandom.current().nextInt(waitMin, waitMax);
            LoggerFactory.getLogger(getClass()).info("Sleeping for " + randomWait + " msec");
            Thread.sleep(randomWait); 
        } catch (InterruptedException ie) {
            LoggerFactory.getLogger(getClass()).info("Interrupted during sleep");
        }
    }
    
    
    
    
}