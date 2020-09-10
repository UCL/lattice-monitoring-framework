package mon.lattice.core;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.control.ProbeLoader;
import mon.lattice.core.plane.DataPlaneMessage;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DefaultControllableDataSource extends BasicDataSource implements ControllableDataSource {
    private int myPID;
    protected long nMeasurements = 0;

    public DefaultControllableDataSource (String dsName) {
        super(dsName);
    } 

    public DefaultControllableDataSource (String dsName, ID id) {
        super(dsName, id);
        // nasty way of getting the PID of the process associated to this Data Source
        // the below string gets the PID splitting PID@hostname
        myPID = Integer.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    } 

    @Override
    public ID addProbe(ProbeLoader p) {  
         addProbe(p.getProbe());
         return p.getProbe().getID();
     } 

    @Override
    public int getMyPID() {
         return myPID;
    }

    @Override
    public void setMyPID(int myPID) {
         this.myPID = myPID;
    }


    /*
    * This Data Source counts the number of messages successfully sent
    */
    @Override
    public void sendSuccess(DataPlaneMessage msg) {
        nMeasurements ++; 
    }
    
    
    /*
    * This Data Source logs the number of messages that were sent before shutting down
    */
    @Override
    protected synchronized void stopQueueHandlingThread() {
        super.stopQueueHandlingThread();
        LoggerFactory.getLogger(DefaultControllableDataSource.class).info("Sent " + nMeasurements + " measurements");
    }


 }
