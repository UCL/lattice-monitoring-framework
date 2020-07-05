package mon.lattice.appl.datasources;

import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DefaultControllableDataSource;
import mon.lattice.core.ID;


public class ZMQControllableDataSource extends DefaultControllableDataSource {

    public ZMQControllableDataSource(String dsName) {
        super(dsName);
    }

    public ZMQControllableDataSource(String dsName, ID id) {
        super(dsName, id);
    }
    
    DataSourceDelegate dataSourceDelegate = getDataSourceDelegate();
    
    
    // when using ZMQ the context termination must be performed before
    // the thread is interrupted
    
    @Override
    public boolean disconnect() {
        boolean disconnected = dataSourceDelegate.disconnect();
        stopQueueHandlingThread();
        return disconnected;
    }
    
    
}
