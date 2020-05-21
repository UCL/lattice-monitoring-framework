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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.management.ControllerAgentInfo;
import mon.lattice.management.Host;
import mon.lattice.management.HostException;
import mon.lattice.management.User;
import mon.lattice.management.UserException;
import mon.lattice.im.delegate.ControllerAgentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.management.ManagementService;
import mon.lattice.management.SessionException;

/**
 *
 * @author uceeftu
 */
public class SSHManager implements ManagementService {    
    final Map<ID, User> users;
    final Map<ID, Host> hosts;
    final Map<ID, SSHSession> sessions;
    
    final Map<ID, DataSourceInfo> dataSources;
    //final Map<ID, Host> dataSourcesHosts;
    
    final Map<ID, DataConsumerInfo> dataConsumers;
    //final Map<ID, Host> dataConsumersHosts;
    
    final Map<ID, ControllerAgentInfo> controllerAgents;
    //final Map<ID, Host> controllerAgentsHosts;
    
    final InfoPlaneDelegate infoPlaneDelegate;
    
    File jarSourceFile;
    File jarDestFile;
    
    Logger LOGGER = LoggerFactory.getLogger(SSHManager.class);
     
    
    public SSHManager(String localJarFilePath, String jarFileName, String remoteJarFilePath, InfoPlaneDelegate info) {
        this.users = new ConcurrentHashMap();
        this.hosts = new ConcurrentHashMap();
        this.sessions = new ConcurrentHashMap();
        
        this.dataSources = new ConcurrentHashMap<>();
        this.dataConsumers = new ConcurrentHashMap<>();
        this.controllerAgents = new ConcurrentHashMap<>();
        
        //this.dataSourcesHosts = new ConcurrentHashMap<>(); 
        //this.dataConsumersHosts = new ConcurrentHashMap<>();
        //this.controllerAgentsHosts = new ConcurrentHashMap();
        
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
            infoPlaneDelegate.addDataSource(dataSource, host, 5000);

            // if there is no Exception before we can now try to get the Data Source PID
            dataSource.setpID(infoPlaneDelegate.getDSPIDFromID(dataSource.getId()));

            dataSource.setRunning();
            dataSource.setStartedTime();
            
            host.addDataSource(dataSource.getId());

            dataSources.put(dataSource.getId(), dataSource);
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
            infoPlaneDelegate.addDataConsumer(dataConsumer, host, 5000);

            // if there is no Exception before we can now try to get the Data Consumer PID
            dataConsumer.setpID(infoPlaneDelegate.getDCPIDFromID(dataConsumer.getId()));

            dataConsumer.setRunning();
            dataConsumer.setStartedTime();
            
            host.addDataConsumer(dataConsumer.getId());

            dataConsumers.put(dataConsumer.getId(), dataConsumer);
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
            infoPlaneDelegate.addControllerAgent(controllerAgent, host, 5000);

            // if there is no Exception before we can now try to get the Controller Agent PID
            controllerAgent.setpID(infoPlaneDelegate.getControllerAgentPIDFromID(controllerAgent.getId()));

            controllerAgent.setRunning();
            controllerAgent.setStartedTime();
            
            host.addControllerAgent(controllerAgent.getId());

            controllerAgents.put(controllerAgent.getId(), controllerAgent);
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
        DataSourceInfo dataSource = dataSources.get(dataSourceID);
        if (dataSource == null) 
            throw new ManagementException(new DSNotFoundException("ID " + dataSourceID + " is not a valid Data Source ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
                
        try { 
            session.stopEntity(dataSource);
            host.removeDataSource(dataSourceID);
            LOGGER.info("Stopped Data Source: " + dataSourceID);
            return (dataSources.remove(dataSourceID) != null);
        } catch (SessionException e) {
            throw new ManagementException(e);
        }
        
    }

    
    @Override
    public boolean stopDataConsumer(ID dataConsumerID, ID sessionID) throws ManagementException {
        DataConsumerInfo dataConsumer = dataConsumers.get(dataConsumerID);
        if (dataConsumer == null) 
            throw new ManagementException(new DCNotFoundException("ID " + dataConsumerID + " is not a valid Data Consumer ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(dataConsumer);
            host.removeDataConsumer(dataConsumerID);
            LOGGER.info("Stopped Data Consumer: " + dataConsumerID);
            return (dataSources.remove(dataConsumerID) != null);
        } catch (SessionException e) {
            throw new ManagementException(e);
        }
    }

    
    @Override
    public boolean stopControllerAgent(ID caID, ID sessionID) throws ManagementException {
        ControllerAgentInfo controllerAgent = controllerAgents.get(caID);
        if (controllerAgent == null) 
            throw new ManagementException(new ControllerAgentNotFoundException("ID " + caID + " is not a valid Controller Agent ID"));
        
        SSHSession session = sessions.get(sessionID);
        if (session == null) 
            throw new ManagementException(new SessionException("ID " + sessionID + " is not a valid Session ID"));
        
        Host host = session.getHost();
        
        try { 
            session.stopEntity(controllerAgent);
            host.removeControllerAgent(caID);
            LOGGER.info("Stopped Controller Agent: " + caID);
            return (controllerAgents.remove(caID) != null);
        } catch (SessionException e) {
            throw new ManagementException(e);
        }
    }

    
}
