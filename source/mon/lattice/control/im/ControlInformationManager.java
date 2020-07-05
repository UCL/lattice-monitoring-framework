package mon.lattice.control.im;

import java.io.IOException;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.MessageType;
import java.util.Map;
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
    public void notifyAnnounceEvent(AbstractAnnounceMessage m) {
        try {
            if (m.getMessageType() == MessageType.ANNOUNCE)
                addAnnouncedEntity(m);
            else if ((m.getMessageType() == MessageType.DEANNOUNCE))
                removeDeannouncedEntity(m);
        } catch (InterruptedException e) {
            LOGGER.info("Thread Interrupted");   
        }
    }
    
    
    private void addAnnouncedEntity(AbstractAnnounceMessage m) throws InterruptedException {
        EntityType type = m.getEntityType();
        ID id = m.getEntityID();
        int replyTimeout = m.getReplyTimeout();
        
        if (type != null) switch (type) {
            case DATASOURCE:
                addDataSource(id, replyTimeout);
                break;
            case DATACONSUMER:
                addDataConsumer(id, replyTimeout);
                break;
            case CONTROLLERAGENT:
                addControllerAgent(id, replyTimeout);
                break;
            case PROBE:
                addProbe(id, replyTimeout);
                break;
            default:
                break;
        } 
    }
    
    
    
    private void removeDeannouncedEntity(AbstractAnnounceMessage m) throws InterruptedException {
        EntityType type = m.getEntityType();
        ID id = m.getEntityID();
        int replyTimeout = m.getReplyTimeout();
        
        if (type != null) switch (type) {
            case DATASOURCE:
                removeDataSource(id, replyTimeout);
                break;
            case DATACONSUMER:
                removeDataConsumer(id, replyTimeout);
                break;
            case CONTROLLERAGENT:
                removeControllerAgent(id, replyTimeout);
                break; 
            case PROBE:
                removeProbe(id, replyTimeout);
                break;
            default:
                break;
        }
    }
    
    
    private void addDataSource(ID id, int timeout) throws InterruptedException {
        if (pendingAddedDataSources.containsKey(id)) {
            LOGGER.debug(Thread.currentThread().getName() + ": Notifying pending Data Source: " + id);
            CountDownLatch latch = pendingAddedDataSources.remove(id);
            latch.countDown();
            LOGGER.info("Added Data Source " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedDataSources.putIfAbsent(id, latch);
            LOGGER.debug(Thread.currentThread().getName() + ": addDataSource: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    
    private void addDataConsumer(ID id, int timeout) throws InterruptedException {
        if (pendingAddedDataConsumers.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Consumer: " + id);
            CountDownLatch latch = pendingAddedDataConsumers.remove(id);
            latch.countDown();
            LOGGER.info("Added Data Consumer " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedDataConsumers.putIfAbsent(id, latch);
            LOGGER.debug("addDataConsumer: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    private void addControllerAgent(ID id, int timeout) throws InterruptedException {
        if (pendingAddedControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            CountDownLatch latch = pendingAddedControllerAgents.remove(id);
            latch.countDown();
            LOGGER.info("Added Controller Agent " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedControllerAgents.putIfAbsent(id, latch);
            LOGGER.debug("addControllerAgent: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    private void addProbe(ID id, int timeout) throws InterruptedException {
        if (pendingAddedProbes.containsKey(id)) {
            LOGGER.debug("Notifying pending Probe: " + id);
            CountDownLatch latch = pendingAddedProbes.remove(id);
            latch.countDown();
            LOGGER.info("Added Probe " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingAddedProbes.putIfAbsent(id, latch);
            LOGGER.debug("addProbe: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    
    
    private void removeDataSource(ID id, int timeout) throws InterruptedException {
        if (pendingRemovedDataSources.containsKey(id)) {
            LOGGER.debug(Thread.currentThread().getName() + ": Notifying pending Data Source: " + id);
            CountDownLatch latch = pendingRemovedDataSources.remove(id);
            latch.countDown();
            LOGGER.info(Thread.currentThread().getName() + ": Removed Data Source " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedDataSources.putIfAbsent(id, latch);
            LOGGER.debug(Thread.currentThread().getName() + ": removeDataSource: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    
    private void removeDataConsumer(ID id, int timeout) throws InterruptedException {
        if (pendingRemovedDataConsumers.containsKey(id)) {
            LOGGER.debug(Thread.currentThread().getName() + ": Notifying pending Data Consumer: " + id);
            CountDownLatch latch = pendingRemovedDataConsumers.remove(id);
            latch.countDown();
            LOGGER.info(Thread.currentThread().getName() + ": Removed Data Consumer " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedDataConsumers.putIfAbsent(id, latch);            
            LOGGER.debug(Thread.currentThread().getName() + ": removeDataConsumer: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    private void removeControllerAgent(ID id, int timeout) throws InterruptedException {
        if (pendingRemovedControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            CountDownLatch latch = pendingRemovedControllerAgents.remove(id);
            latch.countDown();
            LOGGER.info("Removed Controller Agent " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedControllerAgents.putIfAbsent(id, latch);
            LOGGER.debug("removeControllerAgent: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
        }
    }
    
    
    private void removeProbe(ID id, int timeout) throws InterruptedException {
        if (pendingRemovedProbes.containsKey(id)) {
            LOGGER.debug("Notifying pending Probe: " + id);
            CountDownLatch latch = pendingRemovedProbes.remove(id);
            latch.countDown();
            LOGGER.info("Removed Probe " + id.toString());
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            pendingRemovedProbes.putIfAbsent(id, latch);
            LOGGER.debug("removeProbe: Waiting on the latch: " + id);
            latch.await(timeout, TimeUnit.SECONDS);
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
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " was de-announced");
            
            LOGGER.debug("Found this data source ID: " + dataSourceID);
            AbstractControlEndPointMetaData dsAddress = getDSAddressFromID(dataSourceID);
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
    public AbstractControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException {
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
    public AbstractControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was not found in the infoplane");
        
        return this.fetchDataConsumerControlEndPoint(dataConsumer);    
    }
    
    @Override
    public AbstractControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
            if (!containsDataConsumer(dataConsumerID))
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " was de-announced");
                
            LOGGER.debug("Found this data consumer ID: " + dataConsumerID);
            AbstractControlEndPointMetaData dcAddress = getDCAddressFromID(dataConsumerID);
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
    public AbstractControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgentID) throws ControllerAgentNotFoundException, IOException {
        if (!containsControllerAgent(controllerAgentID))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID.toString() + " was not found in the infoplane");
        
        return this.fetchControllerAgentControlEndPoint(controllerAgentID);    
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
