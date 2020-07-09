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
    
    
    public void deployJar() throws SftpException, JSchException   {
        synchronized(host) {
            if (host.isJarDeployed())
                return;
            
            ChannelSftp channelSftp = null;

            Channel channel = SSHSessionHandler.openChannel("sftp");
            channel.connect(3000);
            channelSftp = (ChannelSftp) channel;
            channelSftp.put(host.getJarSource().toString(), host.getJarDestination().toString(), ChannelSftp.OVERWRITE);

            host.setJarDeploymentDate();
            host.setJarDeployed();

            channelSftp.disconnect();
        }
    }
    
    
    @Override
    public synchronized void startEntity(LatticeEntityInfo entity) throws SessionException {
        String jvm = "java"; //we assume the executable is in the PATH
        String command = jvm +
                         " -cp " + host.getJarDestination().toString() + " " + 
                         entity.getEntityClassName() + " " +   
                         entity.getId() + " " +
                         entity.getArguments() + "&";
        
        LOGGER.debug(command);
       
        try {
            Channel channel = SSHSessionHandler.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect(3000);
            channel.disconnect();
        } catch (JSchException e) {
            throw new SessionException(e);
        }
    }  
    
    
    @Override
    public synchronized void stopEntity(LatticeEntityInfo entity) throws SessionException {
        String command = "kill " + entity.getpID();
        LOGGER.debug(command);
        
        try {
            Channel channel = SSHSessionHandler.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect(3000);
            channel.disconnect();
        } catch (JSchException e) {
            throw new SessionException(e);
        }
    }
        
        
        
}
