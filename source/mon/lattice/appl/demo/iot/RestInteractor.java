package mon.lattice.appl.demo.iot;

import eu.fivegex.monitoring.test.LatticeTest;
import java.io.IOException;
import java.util.Properties;

public class RestInteractor extends LatticeTest {
    public RestInteractor(Properties configuration) throws IOException {
        super(configuration);
    }
    
    
    public String getDCDataPlanePort() {
        return super.DCDataPlanePort;
    }

    public void setDCDataPlanePort(String DCDataPlanePort) {
        super.DCDataPlanePort = DCDataPlanePort;
    }

}