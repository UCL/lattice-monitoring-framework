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
    
    private Map<ID, CountDownLatch> pendingAddedProbes;
    private Map<ID, CountDownLatch> pendingAddedDataSources;
    private Map<ID, CountDownLatch> pendingAddedDataConsumers;
    private Map<ID, CountDownLatch> pendingAddedControllerAgents;
    
    private Map<ID, CountDownLatch> pendingRemovedProbes;
    private Map<ID, CountDownLatch> pendingRemovedDataSources;
    private Map<ID, CountDownLatch> pendingRemovedDataConsumers;
    private Map<ID, CountDownLatch> pendingRemovedControllerAgents;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ControlInformationManager.class);
    
    
    
    public ControlInformationManager(InfoPlane info){
        this.info=info;
        
        pendingAddedProbes = new ConcurrentHashMap<>();
        pendingAddedDataSources = new ConcurrentHashMap<>();
        pendingAddedDataConsumers = new ConcurrentHashMap<>();
        pendingAddedControllerAgents = new ConcurrentHashMap<>();
        
        pendingRemovedProbes = new ConcurrentHashMap<>();
        pendingRemovedDataSources = new ConcurrentHashMap<>();
        pendingRemovedDataConsumers = new ConcurrentHashMap<>();
        pendingRemovedControllerAgents = new ConcurrentHashMap<>();
    }
    
    
    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        if (m.getMessageType() == MessageType.ANNOUNCE)
            addAnnouncedEntity(m.getEntityID(), m.getEntity());
        else if ((m.getMessageType() == MessageType.DEANNOUNCE))
                removeDeannouncedEntity(m.getEntityID(), m.getEntity());  
    }
    
    
    private void addAnnouncedEntity(ID id, EntityType type) {
        if (type != null) switch (type) {
            case DATASOURCE:
                notifyAddedDataSource(id);
                LOGGER.info("Added Data Source " + id.toString());
                break;
            case DATACONSUMER:
                notifyAddedDataConsumer(id);
                LOGGER.info("Added Data Consumer " + id.toString());
                break;
            case CONTROLLERAGENT:
                notifyAddedControllerAgent(id);
                LOGGER.info("Added Controller Agent " + id.toString());
                break;
            case PROBE:
                notifyAddedProbe(id);
                LOGGER.info("Added Probe " + id.toString());
                break;
            default:
                break;
        } 
    }
    
    
    
    private void removeDeannouncedEntity(ID id, EntityType type) {
        if (type != null) switch (type) {
            case DATASOURCE:
                notifyRemovedDataSource(id);
                LOGGER.info("Removing Data Source " + id.toString());
                break;
            case DATACONSUMER:
                notifyRemovedDataConsumer(id);
                LOGGER.info("Removing Data Consumer " + id.toString());
                break;
            case CONTROLLERAGENT:
                notifyRemovedControllerAgent(id);
                LOGGER.info("Removing Controller Agent " + id.toString());
                break; 
            case PROBE:
                notifyRemovedProbe(id);
                LOGGER.info("Removing Probe " + id.toString());
                break;
            default:
                break;
        }
    }
    
    
    
    private boolean containsDataSource(ID id) {
        return info.containsDataSource(id, 5);
    } 
    
    private boolean containsDataConsumer(ID id) {
        return info.containsDataConsumer(id, 5);
    }
    
    private boolean containsControllerAgent(ID id) {
        return info.containsControllerAgent(id, 5);
    }
    
    private boolean containsProbe(ID id) {
        return info.containsProbe(id, 5);
    } 
    
    
    @Override
    public void waitForAddedDataSource(DataSourceInfo dataSource, int timeout) throws InterruptedException {
        ID dataSourceID = dataSource.getId();
        
        if (!containsDataSource(dataSourceID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedDataSources.putIfAbsent(dataSourceID, latch);
            LOGGER.debug("waitForDataSource: Waiting on the latch: " + dataSourceID);
            latch.await(timeout, TimeUnit.SECONDS);
        }

    }
    
    
    @Override
    public void waitForAddedDataConsumer(DataConsumerInfo dataConsumer, int timeout) throws InterruptedException {
        ID dataConsumerID = dataConsumer.getId();
        
        if (!containsDataSource(dataConsumerID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedDataConsumers.putIfAbsent(dataConsumerID, latch);
            LOGGER.debug("waitForDataConsumer: Waiting on the latch: " + dataConsumerID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    

    @Override
    public void waitForAddedControllerAgent(ControllerAgentInfo controllerAgent, int timeout) throws InterruptedException {
        ID controllerAgentID = controllerAgent.getId();
        
        if (!containsDataSource(controllerAgentID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedControllerAgents.putIfAbsent(controllerAgentID, latch);
            LOGGER.debug("waitForControllerAgent: Waiting on the latch: " + controllerAgentID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
        
    @Override
    public void waitForAddedProbe(ID probeID, int timeout) throws InterruptedException {
        if (!containsProbe(probeID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedProbes.putIfAbsent(probeID, latch);
            LOGGER.debug("waitForProbe: Waiting on the latch: " + probeID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    void notifyAddedDataSource(ID id) {
        if (pendingAddedDataSources.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Source: " + id);
            CountDownLatch latch = pendingAddedDataSources.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyAddedDataConsumer(ID id) {
        if (pendingAddedDataConsumers.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Consumer: " + id);
            CountDownLatch latch = pendingAddedDataConsumers.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyAddedControllerAgent(ID id) {
        if (pendingAddedControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            CountDownLatch latch = pendingAddedControllerAgents.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyAddedProbe(ID id) {
        if (pendingAddedProbes.containsKey(id)) {
            LOGGER.debug("Notifying pending Probe: " + id);
            CountDownLatch latch = pendingAddedProbes.remove(id);
            latch.countDown();
        }
    }

    
    @Override
    public void waitForRemovedDataSource(DataSourceInfo dataSource, int timeout) throws InterruptedException {
        ID dataSourceID = dataSource.getId();
        
        if (containsDataSource(dataSourceID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedDataSources.putIfAbsent(dataSourceID, latch);
            LOGGER.debug("waitForDataSource: Waiting on the latch: " + dataSourceID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
        // else: already removed from the info plane – no need to wait
    }

    @Override
    public void waitForRemovedDataConsumer(DataConsumerInfo dataConsumer, int timeout) throws InterruptedException {
        ID dataConsumerID = dataConsumer.getId();
        
        if (containsDataSource(dataConsumerID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedDataConsumers.putIfAbsent(dataConsumerID, latch);
            LOGGER.debug("waitForDataConsumer: Waiting on the latch: " + dataConsumerID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void waitForRemovedControllerAgent(ControllerAgentInfo controllerAgent, int timeout) throws InterruptedException {
        ID controllerAgentID = controllerAgent.getId();
        
        if (containsDataSource(controllerAgentID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedControllerAgents.putIfAbsent(controllerAgentID, latch);
            LOGGER.debug("waitForControllerAgent: Waiting on the latch: " + controllerAgentID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void waitForRemovedProbe(ID probeID, int timeout) throws InterruptedException {
        if (containsProbe(probeID)) {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedProbes.putIfAbsent(probeID, latch);
            LOGGER.debug("waitForProbe: Waiting on the latch: " + probeID);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    void notifyRemovedDataSource(ID id) {
        if (pendingRemovedDataSources.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Source: " + id);
            CountDownLatch latch = pendingRemovedDataSources.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyRemovedDataConsumer(ID id) {
        if (pendingRemovedDataConsumers.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Consumer: " + id);
            CountDownLatch latch = pendingRemovedDataConsumers.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyRemovedControllerAgent(ID id) {
        if (pendingRemovedControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            CountDownLatch latch = pendingRemovedControllerAgents.remove(id);
            latch.countDown();
        }
    }
    
    
    void notifyRemovedProbe(ID id) {
        if (pendingRemovedProbes.containsKey(id)) {
            LOGGER.debug("Notifying pending Probe: " + id);
            CountDownLatch latch = pendingRemovedProbes.remove(id);
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
