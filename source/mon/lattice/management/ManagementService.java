/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management;

import mon.lattice.management.ssh.AuthType;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface ManagementService {
    
    public ID addUser(String username, AuthType auth, String token) throws ManagementException;
    
    public boolean deleteUser(ID userID) throws ManagementException;
    
    public ID createSession(ID hostID, ID userid) throws ManagementException;
    
    public boolean deleteSession(ID SessionID) throws ManagementException;
    
    public ID addHost(String address, int port) throws ManagementException;
    
    public boolean removeHost(ID hostID) throws ManagementException;
    
    public ID startDataSource(String className, String args, ID sessionID) throws ManagementException;
    
    public boolean stopDataSource(ID dataSourceID, ID sessionID) throws ManagementException;
    
    public ID startDataConsumer(String className, String args, ID sessionID) throws ManagementException; 
    
    public boolean stopDataConsumer(ID dataConsumerID, ID sessionID) throws ManagementException;
    
    public ID startControllerAgent(String className, String args, ID sessionID) throws ManagementException;
    
    public boolean stopControllerAgent(ID caID, ID sessionID) throws ManagementException;
}
