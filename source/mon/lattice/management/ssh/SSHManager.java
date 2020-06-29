package mon.lattice.management.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import mon.lattice.management.ManagementException;
import mon.lattice.management.DataConsumerInfo;
import mon.lattice.management.DataSourceInfo;
import mon.lattice.im.delegate.DCNotFoundException;
import mon.lattice.im.delegate.DSNotFoundException;
import mon.lattice.im.delegate.InfoPlaneDelegate;
import mon.lattice.core.ID;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.im.delegate.ControlEndPointMetaData;
import mon.lattice.management.ControllerAgentInfo;
import mon.lattice.management.Host;
import mon.lattice.management.HostException;
import mon.lattice.management.User;
import mon.lattice.management.UserException;
import mon.lattice.im.delegate.ControllerAgentNotFoundException;
import mon.lattice.im.delegate.SocketControlEndPointMetaData;
import mon.lattice.im.delegate.ZMQControlEndPointMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.management.ManagementService;
import mon.lattice.management.SessionException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class SSHManager implements ManagementService {    
    final Map<ID, User> users;
    final Map<ID, Host> hosts;
    final Map<ID, SSHSession> sessions;
    
    final Map<ID, DataSourceInfo> deployedDataSources;
    
    final Map<ID, DataConsumerInfo> deployedDataConsumers;
    
    final Map<ID, ControllerAgentInfo> deployedControllerAgents;
    
    final InfoPlaneDelegate infoPlaneDelegate;
    
    File jarSourceFile;
    File jarDestFile;
    
    Logger LOGGER = LoggerFactory.getLogger(SSHManager.class);
     
    
    public SSHManager(String localJarFilePath, String jarFileName, String remoteJarFilePath, InfoPlaneDelegate info) {
        this.users = new ConcurrentHashMap();
        this.hosts = new ConcurrentHashMap();
        this.sessions = new ConcurrentHashMap();
        
        this.deployedDataSources = new ConcurrentHashMap<>();
        this.deployedDataConsumers = new ConcurrentHashMap<>();
        this.deployedControllerAgents = new ConcurrentHashMap<>();
        
        this.infoPlaneDelegate = info;
        
        jarSourceFile = new File(localJarFilePath + "/" + jarFileName);
        jarDestFile = new File(remoteJarFilePath + "/" + jarFileName);
    }
    

    @Override
    public ID addUser(String username, AuthType auth, String token) throws ManagementException {
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
            throw new ManagementException(ue);
        }
            
    }
    
    
    
    @Override
    public ID addHost(String address, int port) throws ManagementException {
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
    public ID createSession(ID hostID, ID userid) throws ManagementException {
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
            
            //we add a reference to this Session to the host he is bound to
            host.addSession(session.getId());
            
            sessions.put(session.getId(), session);
            return session.getId();
            
        }  catch (JSchException | SftpException | SessionException e) {
            throw new ManagementException(e); 
        }
    }

    
    @Override
    public ID startDataSource(String className, String args, ID sessionID) throws ManagementException {
        DataSourceInfo dataSource = new DataSourceInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(dataSource);
        
            // we are supposed to wait here until either the announce message sent by the DS 
            // is received from the Announcelistener thread or the timeout is reached (5 secs)
            infoPlaneDelegate.waitForAddedDataSource(dataSource, 5000);

            // if there is no Exception we can now try to get the Data Source PID
            dataSource.setpID(infoPlaneDelegate.getDSPIDFromID(dataSource.getId()));

            dataSource.setRunning();
            dataSource.setStartedTime();
            
            host.addDataSource(dataSource.getId());
            dataSource.setHost(host);

            deployedDataSources.put(dataSource.getId(), dataSource);
            LOGGER.info("Started Data Source: " + dataSource.getId());
        } catch (SessionException | InterruptedException | DSNotFoundException e) {
            throw new ManagementException(e); 
        }
        
        return dataSource.getId(); 
    }
    
    
    @Override
    public ID startDataConsumer(String className, String args, ID sessionID) throws ManagementException {
        DataConsumerInfo dataConsumer = new DataConsumerInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(dataConsumer);

            // we are supposed to wait here until either the announce message sent by the DC 
            // is received from the Announcelistener thread or the timeout is reached (5 secs)
            infoPlaneDelegate.waitForAddedDataConsumer(dataConsumer, 5000);

            // if there is no Exception before we can now try to get the Data Consumer PID
            dataConsumer.setpID(infoPlaneDelegate.getDCPIDFromID(dataConsumer.getId()));

            dataConsumer.setRunning();
            dataConsumer.setStartedTime();
            
            host.addDataConsumer(dataConsumer.getId());
            dataConsumer.setHost(host);

            deployedDataConsumers.put(dataConsumer.getId(), dataConsumer);
            LOGGER.info("Started Data Consumer: " + dataConsumer.getId());
        } catch (SessionException | InterruptedException | DCNotFoundException e) {
            throw new ManagementException(e);
            
        }
        
        return dataConsumer.getId();
        
    }
   
    
    @Override
    public ID startControllerAgent(String className, String args, ID sessionID) throws ManagementException {
        ControllerAgentInfo controllerAgent = new ControllerAgentInfo(className, args);
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try {
            session.startEntity(controllerAgent);
            
            // we are supposed to wait here until either the announce message sent by the Controller Agent 
            // is received from the Announcelistener thread or the timeout is reached (5 secs)
            infoPlaneDelegate.waitForAddedControllerAgent(controllerAgent, 5000);

            // if there is no Exception before we can now try to get the Controller Agent PID
            controllerAgent.setpID(infoPlaneDelegate.getControllerAgentPIDFromID(controllerAgent.getId()));

            controllerAgent.setRunning();
            controllerAgent.setStartedTime();
            
            host.addControllerAgent(controllerAgent.getId());
            controllerAgent.setHost(host);

            deployedControllerAgents.put(controllerAgent.getId(), controllerAgent);
            LOGGER.info("Started Controller Agent: " + controllerAgent.getId());
        } catch (SessionException | InterruptedException | ControllerAgentNotFoundException e) {
            throw new ManagementException(e);
            
        }
        
        return controllerAgent.getId();
        
    }  
    
    
    @Override
    public boolean deleteUser(ID id) throws ManagementException { 
        if (users.remove(id) == null)
            throw new ManagementException(new UserException("ID " + id + " is not a valid user ID"));
        
        LOGGER.info("Deleted user: " + id);
        return true;
    } 
    
        
    @Override
    public boolean deleteSession(ID sessionID) throws ManagementException {
        SSHSession session = sessions.get(sessionID);
        if (session == null)
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid session ID"));
        
        Host host = session.getHost();
        
        session.stopSSHSession();
        host.removeSession(sessionID);
        
        sessions.remove(sessionID);
        return true;
    }
    
    
    @Override
    public boolean removeHost(ID hostID) throws ManagementException {
        Host host = hosts.get(hostID);
        if (host == null)
            throw new ManagementException(new HostException("ID " + hostID + " is not a valid host ID"));
        
        if (!(host.getSessions().isEmpty() && host.getDataSources().isEmpty() && host.getDataConsumers().isEmpty() && host.getControllerAgents().isEmpty()))
            throw new ManagementException(new HostException("Host " + hostID + " cannot be removed (not empty)"));
            
        LOGGER.info("Removed host: " + hostID);
        hosts.remove(hostID);
        return true;
      
    }
    
    
    
    @Override
    public boolean stopDataSource(ID dataSourceID, ID sessionID) throws ManagementException {
        DataSourceInfo dataSource = deployedDataSources.get(dataSourceID);
        if (dataSource == null) 
            throw new ManagementException(new DSNotFoundException("ID " + dataSourceID + " is not a valid Data Source ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
                
        try { 
            session.stopEntity(dataSource);
            host.removeDataSource(dataSourceID);
            infoPlaneDelegate.waitForRemovedDataSource(dataSource, 5000);
            LOGGER.info("Stopped Data Source: " + dataSourceID);
            return (deployedDataSources.remove(dataSourceID) != null);
        } catch (SessionException | InterruptedException e) {
            throw new ManagementException(e);
        }
        
    }

    
    @Override
    public boolean stopDataConsumer(ID dataConsumerID, ID sessionID) throws ManagementException {
        DataConsumerInfo dataConsumer = deployedDataConsumers.get(dataConsumerID);
        if (dataConsumer == null) 
            throw new ManagementException(new DCNotFoundException("ID " + dataConsumerID + " is not a valid Data Consumer ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(dataConsumer);
            host.removeDataConsumer(dataConsumerID);
            infoPlaneDelegate.waitForRemovedDataConsumer(dataConsumer, 5000);
            LOGGER.info("Stopped Data Consumer: " + dataConsumerID);
            return (deployedDataConsumers.remove(dataConsumerID) != null);
        } catch (SessionException | InterruptedException e) {
            throw new ManagementException(e);
        }
    }

    
    @Override
    public boolean stopControllerAgent(ID caID, ID sessionID) throws ManagementException {
        ControllerAgentInfo controllerAgent = deployedControllerAgents.get(caID);
        if (controllerAgent == null) 
            throw new ManagementException(new ControllerAgentNotFoundException("ID " + caID + " is not a valid Controller Agent ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(controllerAgent);
            host.removeControllerAgent(caID);
            infoPlaneDelegate.waitForRemovedControllerAgent(controllerAgent, 5000);
            LOGGER.info("Stopped Controller Agent: " + caID);
            return (deployedControllerAgents.remove(caID) != null);
        } catch (SessionException | InterruptedException e) {
            throw new ManagementException(e);
        }
    }
    
    
    @Override
    public JSONArray getDataSources() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id : deployedDataSources.keySet()) {
            JSONObject dsAddr = new JSONObject();
            JSONObject dataSourceInfo = new JSONObject();
            try {
                ControlEndPointMetaData dsInfo = infoPlaneDelegate.getDSAddressFromID(id);
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
                ControlEndPointMetaData dcInfo = infoPlaneDelegate.getDCAddressFromID(id);
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
                ControlEndPointMetaData controllerAgentEndPointInfo = infoPlaneDelegate.getControllerAgentAddressFromID(id);
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
