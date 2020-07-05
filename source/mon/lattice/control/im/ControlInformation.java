package mon.lattice.control.im;

import java.io.IOException;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AnnounceEventListener;
import us.monoid.json.JSONArray;

/**
 *
 * @author uceeftu
 */
public interface ControlInformation extends AnnounceEventListener {
    
    public AbstractControlEndPointMetaData getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException, IOException;
    
    public AbstractControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException;
    
    public AbstractControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgent) throws ControllerAgentNotFoundException, IOException;
    
    public String getDSIDFromName(String dsName) throws DSNotFoundException;
    
    public AbstractControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException;
    
    public AbstractControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException;
    
    public int getDSPIDFromID(ID dataSource) throws DSNotFoundException;
    
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException;  
    
    public int getControllerAgentPIDFromID(ID controllerAgent) throws ControllerAgentNotFoundException;
    
    public JSONArray getProbesOnDS(ID dataSource) throws DSNotFoundException;
}
