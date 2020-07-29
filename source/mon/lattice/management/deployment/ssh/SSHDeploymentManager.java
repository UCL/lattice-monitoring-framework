package mon.lattice.management.deployment.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import mon.lattice.management.deployment.DeploymentException;
import mon.lattice.management.deployment.DataConsumerInfo;
import mon.lattice.management.deployment.DataSourceInfo;
import mon.lattice.control.im.DCNotFoundException;
import mon.lattice.control.im.DSNotFoundException;
import mon.lattice.core.ID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.control.im.AbstractControlEndPointMetaData;
import mon.lattice.management.deployment.ControllerAgentInfo;
import mon.lattice.management.deployment.Host;
import mon.lattice.management.deployment.HostException;
import mon.lattice.management.deployment.User;
import mon.lattice.management.deployment.UserException;
import mon.lattice.control.im.ControllerAgentNotFoundException;
import mon.lattice.control.im.SocketControlEndPointMetaData;
import mon.lattice.control.im.ZMQControlEndPointMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.management.deployment.SessionException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.control.im.ControlInformation;
import mon.lattice.control.im.ControlInformationInteracter;
import mon.lattice.control.im.LatchTimeoutException;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import mon.lattice.management.deployment.DeploymentService;

/**
 *
 * @author uceeftu
 */
public class SSHDeploymentManager implements DeploymentService, ControlInformationInteracter {    
    final Map<ID, User> users;
    final Map<ID, Host> hosts;
    final Map<ID, SSHSession> sessions;
    
    final Map<ID, DataSourceInfo> deployedDataSources;
    
    final Map<ID, DataConsumerInfo> deployedDataConsumers;
    
    final Map<ID, ControllerAgentInfo> deployedControllerAgents;
    
    ControlInformation controlInformation;
    
    File jarSourceFile;
    File jarDestFile;
    
    Logger LOGGER = LoggerFactory.getLogger(SSHDeploymentManager.class);
     
    
    public SSHDeploymentManager(String localJarFilePath, String jarFileName, String remoteJarFilePath) {
        this.users = new ConcurrentHashMap();
        this.hosts = new ConcurrentHashMap();
        this.sessions = new ConcurrentHashMap();
        
        this.deployedDataSources = new ConcurrentHashMap<>();
        this.deployedDataConsumers = new ConcurrentHashMap<>();
        this.deployedControllerAgents = new ConcurrentHashMap<>();
        
        jarSourceFile = new File(localJarFilePath + "/" + jarFileName);
        jarDestFile = new File(remoteJarFilePath + "/" + jarFileName);
    }

    @Override
    public ControlInformation getControlInformation() {
        return controlInformation;
    }

    @Override
    public void setControlInformation(ControlInformation ci) {
        this.controlInformation = ci;
    }
    
    
    
    
    

    @Override
    public ID addUser(String username, AuthType auth, String token) throws DeploymentException {
        try {
            if (auth == AuthType.KEY) {
                SSHUserWithKey user = new SSHUserWithKey(username, token);
                users.put(user.getId(), user);
                LOGGER.info("Added User: " + user.getId());
                return user.getId();
            }

            else 
                throw new UserException("The only supported auth method is currently KEY");
            
        } catch (UserException ue) {
            throw new DeploymentException(ue);
        }
            
    }
    
    
    
    @Override
    public ID addHost(String address, int port) throws DeploymentException {
        SSHHost host = SSHHost.getInstance(address, port);
        ID id = host.getId();
        
        if (hosts.containsKey(id)) 
            LOGGER.info("Host already exists: " + id);
        else {
            host.setJarDeploymentInfo(jarSourceFile, jarDestFile);
            hosts.put(id, host);
            LOGGER.info("Added Host: " + id);
        }
        
        return id;
    }
    
    
    @Override
    public ID createSession(ID hostID, ID userid) throws DeploymentException {
        Host host = hosts.get(hostID);
        User user = users.get(userid);
        
        try {
            if (host == null)
                throw new SessionException("The provided host ID " + hostID + " is not valid");
            
            if (user == null) 
                throw new SessionException("The provided user ID " + userid + " is not valid");
        
            SSHSession session = new SSHSession(host, user);
        
            session.startSSHSession();
            session.deployJar();
            
            //we add a reference to this Session to the host it is bound to
            host.addSession(session.getId());
            
            sessions.put(session.getId(), session);
            return session.getId();
            
        }  catch (JSchException | SessionException e) {
            throw new DeploymentException(e); 
        }
    }

    
    @Override
    public ID startDataSource(String className, String args, ID sessionID) throws DeploymentException {
        DataSourceInfo dataSource = new DataSourceInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(dataSource);
        
            // we are supposed to wait here until either the announce message sent by the DS 
            // on the Info Plane is received by the Announcelistener thread or the timeout is reached (10 secs)

            AnnounceMessage m = new AnnounceMessage(dataSource.getId(), EntityType.DATASOURCE, 10);
            controlInformation.notifyAnnounceEvent(m);

            // if there is no Exception we can now try to get the Data Source PID
            dataSource.setpID(controlInformation.getDSPIDFromID(dataSource.getId()));

            dataSource.setRunning();
            dataSource.setStartedTime();
            
            host.addDataSource(dataSource.getId());
            dataSource.setHost(host);

            deployedDataSources.put(dataSource.getId(), dataSource);
            LOGGER.info("Started Data Source: " + dataSource.getId());
        } catch (SessionException | DSNotFoundException | LatchTimeoutException e) {
            throw new DeploymentException(e); 
        }
        
        return dataSource.getId(); 
    }
    
    
    @Override
    public ID startDataConsumer(String className, String args, ID sessionID) throws DeploymentException {
        DataConsumerInfo dataConsumer = new DataConsumerInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(dataConsumer);

            // we are supposed to wait here until either the announce message sent by the DC 
            // is received from the Announcelistener thread or the timeout is reached (10 secs)
            
            AnnounceMessage m = new AnnounceMessage(dataConsumer.getId(), EntityType.DATACONSUMER, 10);
            controlInformation.notifyAnnounceEvent(m);

            // if there is no Exception before we can now try to get the Data Consumer PID
            dataConsumer.setpID(controlInformation.getDCPIDFromID(dataConsumer.getId()));

            dataConsumer.setRunning();
            dataConsumer.setStartedTime();
            
            host.addDataConsumer(dataConsumer.getId());
            dataConsumer.setHost(host);

            deployedDataConsumers.put(dataConsumer.getId(), dataConsumer);
            LOGGER.info("Started Data Consumer: " + dataConsumer.getId());
        }  
          catch (SessionException | DCNotFoundException | LatchTimeoutException e) {
            throw new DeploymentException(e);
        } 
        
        return dataConsumer.getId();
        
    }
   
    
    @Override
    public ID startControllerAgent(String className, String args, ID sessionID) throws DeploymentException {
        ControllerAgentInfo controllerAgent = new ControllerAgentInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(controllerAgent);
            
            // we are supposed to wait here until either the announce message sent by the Controller Agent 
            // is received from the Announcelistener thread or the timeout is reached (10 secs)
            
            AnnounceMessage m = new AnnounceMessage(controllerAgent.getId(), EntityType.CONTROLLERAGENT, 10);
            controlInformation.notifyAnnounceEvent(m);

            // if there is no Exception before we can now try to get the Controller Agent PID
            controllerAgent.setpID(controlInformation.getControllerAgentPIDFromID(controllerAgent.getId()));

            controllerAgent.setRunning();
            controllerAgent.setStartedTime();
            
            host.addControllerAgent(controllerAgent.getId());
            controllerAgent.setHost(host);

            deployedControllerAgents.put(controllerAgent.getId(), controllerAgent);
            LOGGER.info("Started Controller Agent: " + controllerAgent.getId());
        } catch (SessionException | ControllerAgentNotFoundException | LatchTimeoutException e) {
            throw new DeploymentException(e);
            
        }
        
        return controllerAgent.getId();
        
    }  
    
    
    @Override
    public boolean deleteUser(ID id) throws DeploymentException { 
        if (users.remove(id) == null)
            throw new DeploymentException(new UserException("ID " + id + " is not a valid user ID"));
        
        LOGGER.info("Deleted user: " + id);
        return true;
    } 
    
        
    @Override
    public boolean deleteSession(ID sessionID) throws DeploymentException {
        SSHSession session = sessions.get(sessionID);
        if (session == null)
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid session ID"));
        
        Host host = session.getHost();
        
        session.stopSSHSession();
        host.removeSession(sessionID);
        
        sessions.remove(sessionID);
        return true;
    }
    
    
    @Override
    public boolean removeHost(ID hostID) throws DeploymentException {
        Host host = hosts.get(hostID);
        if (host == null)
            throw new DeploymentException(new HostException("ID " + hostID + " is not a valid host ID"));
        
        if (!(host.getSessions().isEmpty() && host.getDataSources().isEmpty() && host.getDataConsumers().isEmpty() && host.getControllerAgents().isEmpty()))
            throw new DeploymentException(new HostException("Host " + hostID + " cannot be removed (not empty)"));
        
        hosts.remove(hostID);
        LOGGER.info("Removed host: " + hostID);
        
        return true;
    }
    
    
    
    @Override
    public boolean stopDataSource(ID dataSourceID, ID sessionID) throws DeploymentException {
        DataSourceInfo dataSource = deployedDataSources.get(dataSourceID);
        if (dataSource == null) 
            throw new DeploymentException(new DSNotFoundException("ID " + dataSourceID + " is not a valid Data Source ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        List<DeannounceMessage> probesDeannounce = getProbesToDeannounce(dataSourceID);
   
        try {    
            session.stopEntity(dataSource);
            host.removeDataSource(dataSourceID);
            
            DeannounceMessage dataSourceMessage = new DeannounceMessage(dataSource.getId(), EntityType.DATASOURCE, 10);
            controlInformation.notifyAnnounceEvent(dataSourceMessage);
            
            // now de-announcing all the related probes on the control plane
            for (DeannounceMessage probeMessage : probesDeannounce)
                controlInformation.notifyAnnounceEvent(probeMessage);
            
            LOGGER.info("Stopped Data Source: " + dataSourceID);
            return (deployedDataSources.remove(dataSourceID) != null);
        } catch (SessionException | LatchTimeoutException e) {
            throw new DeploymentException(e);
        }
        
    }
    
    
    private List<DeannounceMessage> getProbesToDeannounce(ID dataSourceID) {
        JSONArray probesOnDS=null;
        List<DeannounceMessage> probesDeannounce = new ArrayList();
        try {  
            probesOnDS = controlInformation.getProbesOnDS(dataSourceID);
            
            for (int i=0; i < probesOnDS.length(); i++) {
                ID probeID = ID.fromString(probesOnDS.getString(i));
                probesDeannounce.add(new DeannounceMessage(probeID, EntityType.PROBE, 0));
            }
            
        } catch(DSNotFoundException dse) {
            LOGGER.info("Could not retrieve probes list for Data Source " + dataSourceID);
        } catch(JSONException je) {
            LOGGER.info("Error while retrieving probe ID");
        }
        return probesDeannounce;
    }

    
    @Override
    public boolean stopDataConsumer(ID dataConsumerID, ID sessionID) throws DeploymentException {
        DataConsumerInfo dataConsumer = deployedDataConsumers.get(dataConsumerID);
        if (dataConsumer == null) 
            throw new DeploymentException(new DCNotFoundException("ID " + dataConsumerID + " is not a valid Data Consumer ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(dataConsumer);
            host.removeDataConsumer(dataConsumerID);
            
            DeannounceMessage m = new DeannounceMessage(dataConsumer.getId(), EntityType.DATACONSUMER, 10);
            controlInformation.notifyAnnounceEvent(m);
            
            LOGGER.info("Stopped Data Consumer: " + dataConsumerID);
            return (deployedDataConsumers.remove(dataConsumerID) != null);
        } catch (SessionException | LatchTimeoutException e) {
            throw new DeploymentException(e);
        }
    }

    
    @Override
    public boolean stopControllerAgent(ID caID, ID sessionID) throws DeploymentException {
        ControllerAgentInfo controllerAgent = deployedControllerAgents.get(caID);
        if (controllerAgent == null) 
            throw new DeploymentException(new ControllerAgentNotFoundException("ID " + caID + " is not a valid Controller Agent ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new DeploymentException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(controllerAgent);
            host.removeControllerAgent(caID);
            
            DeannounceMessage m = new DeannounceMessage(controllerAgent.getId(), EntityType.CONTROLLERAGENT, 10);
            controlInformation.notifyAnnounceEvent(m);
            
            LOGGER.info("Stopped Controller Agent: " + caID);
            return (deployedControllerAgents.remove(caID) != null);
        } catch (SessionException | LatchTimeoutException e) {
            throw new DeploymentException(e);
        }
    }
    
    
    /* TODO: the following methods should iterate over the keys of the 
       ControlInformationManager to include entities not deployed by this
       manager */
    
    @Override
    public JSONArray getDataSources() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id : deployedDataSources.keySet()) {
            JSONObject dsAddr = new JSONObject();
            JSONObject dataSourceInfo = new JSONObject();
            try {
                AbstractControlEndPointMetaData dsInfo = controlInformation.getDSAddressFromID(id);
                if (dsInfo instanceof ZMQControlEndPointMetaData)
                    dsAddr.put("type", ((ZMQControlEndPointMetaData)dsInfo).getType());
                else if (dsInfo instanceof SocketControlEndPointMetaData) {
                    dsAddr.put("host", ((SocketControlEndPointMetaData)dsInfo).getHost().getHostAddress());
                    dsAddr.put("port", ((SocketControlEndPointMetaData)dsInfo).getPort());
                }
                dataSourceInfo.put("id", id.toString());
                dataSourceInfo.put("info", dsAddr);
                
                DataSourceInfo dataSource = deployedDataSources.get(id);
                
                if (dataSource != null) {
                    Host resource = dataSource.getHost();
                    
                    JSONObject deployment = new JSONObject();
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(dataSource.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                    dataSourceInfo.put("deployment", deployment);
                }
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
            }
              catch (DSNotFoundException ex) {
                LOGGER.error(ex.getMessage());
              }
            obj.put(dataSourceInfo);
        }
        return obj;
    }
    
    
    @Override
    public JSONArray getDataConsumers() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id: deployedDataConsumers.keySet()) {
            JSONObject dcAddr = new JSONObject();
            JSONObject dataConsumerInfo = new JSONObject();
            try {
                AbstractControlEndPointMetaData dcInfo = controlInformation.getDCAddressFromID(id);
                if (dcInfo instanceof ZMQControlEndPointMetaData)
                    dcAddr.put("type", ((ZMQControlEndPointMetaData)dcInfo).getType());
                else if (dcInfo instanceof SocketControlEndPointMetaData) {
                    dcAddr.put("host", ((SocketControlEndPointMetaData)dcInfo).getHost().getHostAddress());
                    dcAddr.put("port", ((SocketControlEndPointMetaData)dcInfo).getPort());
                }
                dataConsumerInfo.put("id", id.toString());
                dataConsumerInfo.put("info", dcAddr);
                
                DataConsumerInfo dataConsumer = deployedDataConsumers.get(id);
                
                if (dataConsumer != null) {
                    Host resource = dataConsumer.getHost();
                    
                    JSONObject deployment = new JSONObject();
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(dataConsumer.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                    dataConsumerInfo.put("deployment", deployment);
                }
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
              }
              catch (DCNotFoundException ex) {
                LOGGER.error(ex.getMessage());
              }
            obj.put(dataConsumerInfo);
            }
        return obj;
    }

    @Override
    public JSONArray getControllerAgents() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id: deployedControllerAgents.keySet()) {
            JSONObject controllerAgentAddr = new JSONObject();
            JSONObject controllerAgentInfo = new JSONObject();
            try {
                AbstractControlEndPointMetaData controllerAgentEndPointInfo = controlInformation.getControllerAgentAddressFromID(id);
                if (controllerAgentEndPointInfo instanceof ZMQControlEndPointMetaData)
                    controllerAgentAddr.put("type", ((ZMQControlEndPointMetaData)controllerAgentEndPointInfo).getType());
                else if (controllerAgentEndPointInfo instanceof SocketControlEndPointMetaData) {
                    controllerAgentAddr.put("host", ((SocketControlEndPointMetaData)controllerAgentEndPointInfo).getHost().getHostAddress());
                    controllerAgentAddr.put("port", ((SocketControlEndPointMetaData)controllerAgentEndPointInfo).getPort());
                }
                controllerAgentInfo.put("id", id.toString());
                controllerAgentInfo.put("info", controllerAgentAddr);
                
                ControllerAgentInfo controllerAgent = deployedControllerAgents.get(id);
                
                if (controllerAgent != null) {
                    Host resource = controllerAgent.getHost();
                    
                    JSONObject deployment = new JSONObject();
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(controllerAgent.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                    controllerAgentInfo.put("deployment", deployment);
                }
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
              }
              catch (ControllerAgentNotFoundException ex) {
                LOGGER.error(ex.getMessage());
              }
            obj.put(controllerAgentInfo);
            }
        return obj;
    }
    
    
}

