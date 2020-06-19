package mon.lattice.im.delegate;

import java.io.IOException;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.MessageType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.management.ControllerAgentInfo;
import mon.lattice.management.DataConsumerInfo;
import mon.lattice.management.DataSourceInfo;
import mon.lattice.management.Host;
import mon.lattice.core.plane.InfoPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author uceeftu
 */
public class ControlInformationManager implements InfoPlaneDelegate {
    private final InfoPlane info;
    
    private Map<ID, CountDownLatch> pendingProbes;
    private Map<ID, CountDownLatch> pendingDataSources;
    private Map<ID, CountDownLatch> pendingDataConsumers;
    private Map<ID, CountDownLatch> pendingControllerAgents;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ControlInformationManager.class);
    
    
    
    public ControlInformationManager(InfoPlane info){
        this.info=info;
        
        pendingProbes = new ConcurrentHashMap<>();
        pendingDataSources = new ConcurrentHashMap<>();
        pendingDataConsumers = new ConcurrentHashMap<>();
        pendingControllerAgents = new ConcurrentHashMap<>();
    }
    
    
    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        if (m.getMessageType() == MessageType.ANNOUNCE)
            addAnnouncedEntity(m.getEntityID(), m.getEntity());
        else if ((m.getMessageType() == MessageType.DEANNOUNCE))
                removeDeannouncedEntity(m.getEntityID(), m.getEntity());  
    }
    
    
    void addAnnouncedEntity(ID id, EntityType type) {
        if (type != null) switch (type) {
            case DATASOURCE:
                notifyDataSource(id);
                LOGGER.info("Added Data Source " + id.toString());
                break;
            case DATACONSUMER:
                notifyDataConsumer(id);
                LOGGER.info("Added Data Consumer " + id.toString());
                break;
            case CONTROLLERAGENT:
                notifyControllerAgent(id);
                LOGGER.info("Added Controller Agent " + id.toString());
                break;
            case PROBE:
                notifyProbe(id);
                LOGGER.info("Added Probe " + id.toString());
                break;
            default:
                break;
        } 
    }
    
    
    // we might want to sync this with the control plane methods
    // this is not strictly needed
    
    void removeDeannouncedEntity(ID id, EntityType type) {
//        if (type == EntityType.DATASOURCE) { // && containsDataSource(id)) {
//            LOGGER.info("Removing Data Source " + id.toString());
//            deleteDataSource(id);
//        }
//        else if (type == EntityType.DATACONSUMER && containsDataConsumer(id)) {
//            LOGGER.info("Removing Data Consumer " + id.toString());
//            deleteDataConsumer(id);
//        } else if (type == EntityType.CONTROLLERAGENT && containsControllerAgent(id)) {
//              LOGGER.info("Removing Controller Agent " + id.toString());
//              deleteControllerAgent(id);
//        } else if (type == EntityType.PROBE && containsProbe(id)) {
//              LOGGER.info("Removing Probe " + id.toString());
//              deleteProbe(id); 
//        }
    }
    
    
    
    @Override
    public boolean containsDataSource(ID id) {
        return info.containsDataSource(id, 0);
    }
    
    @Override
    public boolean containsDataConsumer(ID id) {
        return info.containsDataConsumer(id, 0);
    }
    
    @Override
    public boolean containsControllerAgent(ID id) {
        return info.containsControllerAgent(id, 0);
    }
    
    @Override
    public boolean containsProbe(ID id) {
        return info.containsProbe(id, 0);
    } 
    
    
    @Override
    public void waitForDataSource(DataSourceInfo dataSource, Host resource, int timeout) throws InterruptedException, DSNotFoundException {
        ID dataSourceID = dataSource.getId();
        
        if (!containsDataSource(dataSourceID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingDataSources.put(dataSourceID, latch);
            LOGGER.debug("waitForDataSource: Waiting on the latch: " + dataSourceID);
            latch.await(timeout, TimeUnit.SECONDS);
        }

    }
    
    
    @Override
    public void waitForDataConsumer(DataConsumerInfo dataConsumer, Host resource, int timeout) throws InterruptedException, DCNotFoundException {
        ID dataConsumerID = dataConsumer.getId();
        
        if (!containsDataSource(dataConsumerID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingDataConsumers.put(dataConsumerID, latch);
            LOGGER.debug("waitForDataConsumer: Waiting on the latch: " + dataConsumerID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    

    @Override
    public void waitForControllerAgent(ControllerAgentInfo controllerAgent, Host resource, int timeout) throws InterruptedException, ControllerAgentNotFoundException {
        ID controllerAgentID = controllerAgent.getId();
        
        if (!containsDataSource(controllerAgentID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingControllerAgents.put(controllerAgentID, latch);
            LOGGER.debug("waitForControllerAgent: Waiting on the latch: " + controllerAgentID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
        
    @Override
    public void waitForProbe(ID probeID, int timeout) throws InterruptedException, ProbeNotFoundException {
        if (!containsProbe(probeID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingProbes.put(probeID, latch);
            LOGGER.debug("waitForProbe: Waiting on the latch: " + probeID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    void notifyDataSource(ID id) {
        if (pendingDataSources.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Source: " + id);
            CountDownLatch latch = pendingDataSources.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyDataConsumer(ID id) {
        if (pendingDataConsumers.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Consumer: " + id);
            CountDownLatch latch = pendingDataConsumers.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyControllerAgent(ID id) {
        if (pendingControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            CountDownLatch latch = pendingControllerAgents.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyProbe(ID id) {
        if (pendingProbes.containsKey(id)) {
            LOGGER.debug("Notifying pending Probe: " + id);
            CountDownLatch latch = pendingProbes.remove(id);
            latch.countDown();
        }
    }

    
    @Override
    public ControlEndPointMetaData getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException, IOException {
        String dsID = (String)info.lookupProbeInfo(probe, "datasource");
        
        if (dsID != null) {
            ID dataSourceID = ID.fromString(dsID);
            if (!containsDataSource(dataSourceID))
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " was de-announced");
            
            LOGGER.debug("Found this data source ID: " + dataSourceID);
            ControlEndPointMetaData dsAddress = getDSAddressFromID(dataSourceID);
            if (dsAddress != null)
                return dsAddress;
            else
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " not found in the infoplane");
        }
        else {
            LOGGER.error("Probe ID error");
            throw new ProbeNotFoundException("Probe with ID " + probe.toString() + " not found in the infoplane");
        }
    }
    
    @Override
    public ControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was not found in the infoplane");
        
        return this.fetchDataSourceControlEndPoint(dataSource);
        
    }
        
    @Override
    public String getDSIDFromName(String dsName) throws DSNotFoundException {
        //using generic getInfo method for getting DS ID from DS name
        String dsID = (String)info.getInfo("/datasource/name/" + dsName);
        if (dsID != null)
            if (!containsDataSource(ID.fromString(dsID)))
                throw new DSNotFoundException("Data Source with ID " + dsID + " was de-announced");
            else
                return dsID;
        else 
            throw new DSNotFoundException("Data Source with name " + dsName + " not found in the infoplane");
        }  
    
    @Override
    public ControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was not found in the infoplane");
        
        return this.fetchDataConsumerControlEndPoint(dataConsumer);    
    }
    
    @Override
    public ControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
            if (!containsDataConsumer(dataConsumerID))
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " was de-announced");
                
            LOGGER.debug("Found this data consumer ID: " + dataConsumerID);
            ControlEndPointMetaData dcAddress = getDCAddressFromID(dataConsumerID);
            if (dcAddress != null)
                return dcAddress;
            else
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " not found in the infoplane");
        }
        else
            throw new ReporterNotFoundException("Probe with ID " + reporter.toString() + " not found in the infoplane");
    }
    
    @Override
    public int getDSPIDFromID(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataSourceInfo(dataSource, "pid");
        if (pID != null)
            return pID;
        else 
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the infoplane or missing pid entry"); 
    }
    
    @Override
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataConsumerInfo(dataConsumer, "pid");
        if (pID != null)
            return pID;
        else
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the infoplane or missing pid entry");
    }

    @Override
    public int getControllerAgentPIDFromID(ID controllerAgent) throws ControllerAgentNotFoundException {
        if (!containsControllerAgent(controllerAgent))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupControllerAgentInfo(controllerAgent, "pid");
        if (pID != null)
            return pID;
        else
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " not found in the infoplane or missing pid entry");
    }

    @Override
    public JSONArray getProbesOnDS(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        JSONArray probesOnDS = (JSONArray) info.lookupProbesOnDataSource(dataSource);
        return probesOnDS;
    }
    
    @Override
    public ControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgentID) throws ControllerAgentNotFoundException, IOException {
        if (!containsControllerAgent(controllerAgentID))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID.toString() + " was not found in the infoplane");
        
        return this.fetchControllerAgentControlEndPoint(controllerAgentID);    
    }
    
    
    private ControlEndPointMetaData fetchDataSourceControlEndPoint(ID dataSourceID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupDataSourceInfo(dataSourceID, "controlEndPoint");
        return ControlEndPointMetaDataFactory.newInstance(rawControlEndPointInfo, dataSourceID);
    }
    
    private ControlEndPointMetaData fetchDataConsumerControlEndPoint(ID dataConsumerID) throws IOException {
        Object rawControlEndPointInfo = info.lookupDataConsumerInfo(dataConsumerID, "controlEndPoint");
        return ControlEndPointMetaDataFactory.newInstance(rawControlEndPointInfo, dataConsumerID);
    }
    
    private ControlEndPointMetaData fetchControllerAgentControlEndPoint(ID controllerAgentID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupControllerAgentInfo(controllerAgentID, "controlEndPoint");
        return ControlEndPointMetaDataFactory.newInstance(rawControlEndPointInfo, controllerAgentID);
    }   
    
}
