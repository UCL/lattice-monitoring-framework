package mon.lattice.control.im;

import java.io.IOException;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.MessageType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
public class ControlInformationManager implements ControlInformation {
    private final InfoPlane info;
    
    private Map<ID, CountDownLatch> pendingAddedProbes;
    private Map<ID, CountDownLatch> pendingAddedDataSources;
    private Map<ID, CountDownLatch> pendingAddedDataConsumers;
    private Map<ID, CountDownLatch> pendingAddedControllerAgents;
    
    private Map<ID, CountDownLatch> pendingRemovedProbes;
    private Map<ID, CountDownLatch> pendingRemovedDataSources;
    private Map<ID, CountDownLatch> pendingRemovedDataConsumers;
    private Map<ID, CountDownLatch> pendingRemovedControllerAgents;
    
    
    /* these Sets will store entries from the InfoPlane.
     Some implementations of the infoplane do not allow iterating on all
     the entries, this should solve the issue */
    
    private Set<ID> dataSources;
    private Set<ID> dataConsumers;
    private Set<ID> controllerAgents;
    private Set<ID> probes;
    
    
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
        
        dataSources = ConcurrentHashMap.newKeySet();
        dataConsumers = ConcurrentHashMap.newKeySet();
        controllerAgents = ConcurrentHashMap.newKeySet();
        probes =  ConcurrentHashMap.newKeySet();
    }
    
    
    
    
    @Override
    public void notifyAnnounceEvent(AbstractAnnounceMessage m) {
        Map<ID, CountDownLatch> pendingEntities;
        Set<ID> entities;
        
        EntityType entityType = m.getEntityType();
        MessageType messageType = m.getMessageType();
        
        /* in order to use only one method to deal with all the types of entities
           we select the required data structures and then pass a reference to 
           them to the waitOrNotifyEntity method
        */
        
        if (entityType != null) switch (entityType) {
            case DATASOURCE:
                if (messageType == MessageType.ANNOUNCE) 
                    pendingEntities = pendingAddedDataSources;
                else
                    pendingEntities = pendingRemovedDataSources;
                entities = dataSources;
                break;
            case DATACONSUMER:
                if (messageType == MessageType.ANNOUNCE) 
                    pendingEntities = pendingAddedDataConsumers;
                else
                    pendingEntities = pendingRemovedDataConsumers;
                entities = dataConsumers;
                break;
            case CONTROLLERAGENT:
                if (messageType == MessageType.ANNOUNCE) 
                    pendingEntities = pendingAddedControllerAgents;
                else
                    pendingEntities = pendingRemovedControllerAgents;
                entities = controllerAgents;
                break;
            case PROBE:
                if (messageType == MessageType.ANNOUNCE) 
                    pendingEntities = pendingAddedProbes;
                else
                    pendingEntities = pendingRemovedProbes;
                entities = probes;
                break;
            default:
                return;
        } else
            return;
        
        waitOrNotifyEntity(m, pendingEntities, entities);
    }
    

    
    
    private void waitOrNotifyEntity(AbstractAnnounceMessage m, Map<ID, CountDownLatch> pendingEntities, Set<ID> entities) {
        ID id = m.getEntityID();
        int LatchCount;
        
        /* all the Threads will attempt to create a Latch. The first one completing
           the operation will add the Latch to the Map, the other ones will 
           get an handle to it. This is managed via calling putIfAbsent.
        
           In general there are three threads that need synchronization:
           Management, Control and Information. However not all of them need to
           be synchronized with each other for each added / removed entity
        
           During the probe related operations the threads to be synchronized are
           control and information plane. 
           During deannounce operations the threads to be synchronized are the management
           and information as we do not need to wait for the control plane. 
           LatchCount is therefore set to 1.
        */
        
        if (m.getEntityType() == EntityType.PROBE || m.getMessageType() == MessageType.DEANNOUNCE) 
            LatchCount = 1;
        /* During the announce operations not involving probes we need to sync three threads,
           i.e., management, control and information. LatchCount is set to 2 in this case.
        */
        else
            LatchCount = 2;
        
        
        CountDownLatch existingLatch = pendingEntities.putIfAbsent(id, 
                                                                   new CountDownLatch(LatchCount));
        
        /* The thread that first created the Latch will wait on it */
        if (existingLatch == null)
            waitForEntity(m, pendingEntities);
        
        /* The thread(s) that received a handle to the existing Latch will
           notify the other (waiting) thread
        */
        else
            notifyEntity(m, pendingEntities, entities);
    }
    
    
    private void notifyEntity(AbstractAnnounceMessage m, Map<ID, CountDownLatch> pendingEntities, Set<ID> entities) {
        ID id = m.getEntityID();
        EntityType entityType = m.getEntityType();
        MessageType messageType = m.getMessageType();
        
        LOGGER.debug(Thread.currentThread().getName() + ": Notifying pending " + entityType + ": " + id);
        CountDownLatch latch = pendingEntities.get(id);
        latch.countDown();

        if (latch.getCount() == 0) {
            if (messageType == MessageType.ANNOUNCE) {
                entities.add(id);
                LOGGER.info("Added " + entityType + ": " + id.toString());
            } else {
                entities.remove(id);
                LOGGER.info("Removed " + entityType + ": " + id.toString());
            }
        } 
    }
    
    
    private void waitForEntity(AbstractAnnounceMessage m, Map<ID, CountDownLatch> pendingEntities) {
        ID id = m.getEntityID();
        EntityType entityType = m.getEntityType();
        int replyTimeout = m.getReplyTimeout();
        
        CountDownLatch latch = pendingEntities.get(id);
        
        LOGGER.debug(Thread.currentThread().getName() + ": Waiting on the latch: " + id + " for " + entityType);
        try {
            latch.await(replyTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.info("The thread was interrupted while waiting for " + entityType + " " + id);
        }

        /* we send a timeout exception when a timeout was actually set in the 
           announce message (replyTimeout > 0) and the latch LatchCount is still > 0 
        */
        if (replyTimeout > 0 && latch.getCount() > 0) {
            // cleaning up
            pendingEntities.remove(id);
            throw new LatchTimeoutException("The thread reached a timeout waiting for " + entityType + " " + id);
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
    
    

    @Override
    public AbstractControlEndPointMetaData getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException, IOException {
        String dsID = (String)info.lookupProbeInfo(probe, "datasource");
        
        if (dsID != null) {
            ID dataSourceID = ID.fromString(dsID);
            if (!containsDataSource(dataSourceID))
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " not found in the Info Plane");
            
            LOGGER.debug("Found this data source ID: " + dataSourceID);
            AbstractControlEndPointMetaData dsAddress = getDSAddressFromID(dataSourceID);
            if (dsAddress != null)
                return dsAddress;
            else
                throw new DSNotFoundException("Address of Data Source with ID " + dataSourceID.toString() + " could not be found");
        }
        else {
            LOGGER.error("Probe ID error");
            throw new ProbeNotFoundException("Probe with ID " + probe.toString() + " not found in the infoplane");
        }
    }
    
    @Override
    public AbstractControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the Info Plane");
        
        return this.fetchDataSourceControlEndPoint(dataSource);
        
    }
        
    @Override
    public String getDSIDFromName(String dsName) throws DSNotFoundException {
        //using generic getInfo method for getting DS ID from DS name
        String dsID = (String)info.getInfo("/datasource/name/" + dsName);
        if (dsID != null)
            if (!containsDataSource(ID.fromString(dsID)))
                throw new DSNotFoundException("Data Source with ID " + dsID + " not found in the Info Plane");
            else
                return dsID;
        else 
            throw new DSNotFoundException("Data Source with name " + dsName + " not found in the Info Plane");
        }  
    
    @Override
    public AbstractControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the Info Plane");
        
        return this.fetchDataConsumerControlEndPoint(dataConsumer);    
    }
    
    @Override
    public AbstractControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
            if (!containsDataConsumer(dataConsumerID))
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " not found in the Info Plane");
                
            LOGGER.debug("Found this data consumer ID: " + dataConsumerID);
            AbstractControlEndPointMetaData dcAddress = getDCAddressFromID(dataConsumerID);
            if (dcAddress != null)
                return dcAddress;
            else
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " not found in the Info Plane");
        }
        else
            throw new ReporterNotFoundException("Probe with ID " + reporter.toString() + " not found in the Info Plane");
    }
    
    @Override
    public int getDSPIDFromID(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the Info Plane");
        
        Integer pID = (Integer)info.lookupDataSourceInfo(dataSource, "pid");
        if (pID != null)
            return pID;
        else 
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the Info Plane or missing pid entry"); 
    }
    
    @Override
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the Info Plane");
        
        Integer pID = (Integer)info.lookupDataConsumerInfo(dataConsumer, "pid");
        if (pID != null)
            return pID;
        else
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the Info Plane or missing pid entry");
    }

    @Override
    public int getControllerAgentPIDFromID(ID controllerAgent) throws ControllerAgentNotFoundException {
        if (!containsControllerAgent(controllerAgent))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " not found in the Info Plane");
        
        Integer pID = (Integer)info.lookupControllerAgentInfo(controllerAgent, "pid");
        if (pID != null)
            return pID;
        else
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " not found in the Info Plane or missing pid entry");
    }

    @Override
    public JSONArray getProbesOnDS(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the Info Plane");
        
        JSONArray probesOnDS = (JSONArray) info.lookupProbesOnDataSource(dataSource);
        return probesOnDS;
    }
    
    @Override
    public AbstractControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgentID) throws ControllerAgentNotFoundException, IOException {
        if (!containsControllerAgent(controllerAgentID))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID.toString() + " not found in the Info Plane");
        
        return this.fetchControllerAgentControlEndPoint(controllerAgentID);    
    }
    

    @Override
    public Set<ID> getDataSources() {
        return dataSources;
    }

    @Override
    public Set<ID> getDataConsumers() {
        return dataConsumers;
    }

    @Override
    public Set<ID> getControllerAgents() {
        return controllerAgents;
    }
    
    
    
    private AbstractControlEndPointMetaData fetchDataSourceControlEndPoint(ID dataSourceID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupDataSourceInfo(dataSourceID, "controlEndPoint");
        return AbstractControlEndPointMetaData.newInstance(rawControlEndPointInfo, dataSourceID);
    }
    
    private AbstractControlEndPointMetaData fetchDataConsumerControlEndPoint(ID dataConsumerID) throws IOException {
        Object rawControlEndPointInfo = info.lookupDataConsumerInfo(dataConsumerID, "controlEndPoint");
        return AbstractControlEndPointMetaData.newInstance(rawControlEndPointInfo, dataConsumerID);
    }
    
    private AbstractControlEndPointMetaData fetchControllerAgentControlEndPoint(ID controllerAgentID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupControllerAgentInfo(controllerAgentID, "controlEndPoint");
        return AbstractControlEndPointMetaData.newInstance(rawControlEndPointInfo, controllerAgentID);
    }   
    
}
