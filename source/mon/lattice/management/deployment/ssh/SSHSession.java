/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment.ssh;

import mon.lattice.management.deployment.AbstractSession;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import mon.lattice.core.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.management.deployment.Host;
import mon.lattice.management.deployment.LatticeEntityInfo;
import mon.lattice.management.deployment.SessionException;
import mon.lattice.management.deployment.User;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public class SSHSession extends AbstractSession {
    static final JSch jsch = new JSch();
    
    Session SSHSessionHandler;
    
    
    Logger LOGGER = LoggerFactory.getLogger(SSHSession.class);
       
    public SSHSession(Host host, User user) {
        super(host, user);
        id = ID.generate();
    }

    
    
    public Session getSSHSessionHandler() {
        return SSHSessionHandler;
    }
    
    
    public void startSSHSession() throws JSchException {
        if (user instanceof SSHUserWithKey) {
            jsch.addIdentity(((SSHUserWithKey) user).getIdentityFile().toString());
        }

        SSHSessionHandler = jsch.getSession(user.getUsername(), host.getAddress(), host.getPort());
        SSHSessionHandler.setConfig("PreferredAuthentications", "publickey");
        SSHSessionHandler.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        SSHSessionHandler.setServerAliveInterval(5*60*1000); // 5 minutes
        SSHSessionHandler.connect(3000);
        LOGGER.info("Created Session using: " + user.getUsername() + " and " + ((SSHUserWithKey) user).getIdentityFile());
    }
    
    
    public void stopSSHSession() {
        LOGGER.info("Terminating session with ID " + id);
        SSHSessionHandler.disconnect();
    }
    
    
    public void deployJar() throws SessionException {
        synchronized(host) {
            if (host.isJarDeployed())
                return;
            
            ChannelSftp channelSftp = null;
            try {
                Channel channel = SSHSessionHandler.openChannel("sftp");
                channel.connect(3000);
                channelSftp = (ChannelSftp) channel;
                channelSftp.put(host.getJarSource().toString(), host.getJarDestination().toString(), ChannelSftp.OVERWRITE);

                host.setJarDeploymentDate();
                host.setJarDeployed();
            } catch (SftpException | JSchException e) {
                throw new SessionException(e);
            } finally {
                if (channelSftp != null) channelSftp.disconnect();
            }
        }
    }
    
    
    @Override
    public void startEntity(LatticeEntityInfo entity) throws SessionException {
        String jvm = "java"; //we assume the executable is in the PATH
        String command = jvm +
                         " -cp " + host.getJarDestination().toString() + " " + 
                         entity.getEntityClassName() + " " +   
                         entity.getId() + " " +
                         entity.getArguments() + "&";
        
        LOGGER.debug(command);
       
        Channel channel = null;
        ChannelSftp channelSftp = null;
                
        try {
            channel = SSHSessionHandler.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect(3000);
            channel.disconnect();
            
            /* now checking that the process was started */
            channel = SSHSessionHandler.openChannel("sftp");
            channel.connect(3000);
            channelSftp = (ChannelSftp) channel;
            
            Thread.sleep(500);
            
            String entityType;
            if (entity.getEntityType() == EntityType.DATASOURCE)
                entityType = "data-source-";
            else if (entity.getEntityType() == EntityType.DATACONSUMER)
                entityType = "data-consumer-";
            else
                return;
            
            String entityLogFile = "/tmp/" + entityType + entity.getId().toString() + ".log";
            channelSftp.stat(entityLogFile);
            
        } catch (SftpException sftpe) {
            throw new SessionException("Error while starting the " + entity.getEntityType() + " process with command: " + command);
        } catch (JSchException | InterruptedException e) {
            throw new SessionException(e);
        } finally {
            if (channel != null) channel.disconnect();
            if (channelSftp != null) channelSftp.disconnect();
        }
    }  
    
    
    @Override
    public void stopEntity(LatticeEntityInfo entity) throws SessionException {
        String command = "kill " + entity.getpID();
        LOGGER.debug(command);
        
        Channel channel = null;
        
        try {
            channel = SSHSessionHandler.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect(3000);
            channel.disconnect();
        } catch (JSchException e) {
            throw new SessionException(e);
        } finally {
            if (channel != null) channel.disconnect();
        }
    }
        
        
        
}
