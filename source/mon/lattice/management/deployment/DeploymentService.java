/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment;

import mon.lattice.management.deployment.ssh.AuthType;
import mon.lattice.core.ID;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

/**
 *
 * @author uceeftu
 */
public interface DeploymentService {
    
    public ID addUser(String username, AuthType auth, String token) throws DeploymentException;
    
    public boolean deleteUser(ID userID) throws DeploymentException;
    
    public ID createSession(ID hostID, ID userid) throws DeploymentException;
    
    public boolean deleteSession(ID SessionID) throws DeploymentException;
    
    public ID addHost(String address, int port) throws DeploymentException;
    
    public boolean removeHost(ID hostID) throws DeploymentException;
    
    public ID startDataSource(String className, String args, ID sessionID) throws DeploymentException;
    
    public boolean stopDataSource(ID dataSourceID, ID sessionID) throws DeploymentException;
    
    public ID startDataConsumer(String className, String args, ID sessionID) throws DeploymentException; 
    
    public boolean stopDataConsumer(ID dataConsumerID, ID sessionID) throws DeploymentException;
    
    public ID startControllerAgent(String className, String args, ID sessionID) throws DeploymentException;
    
    public boolean stopControllerAgent(ID caID, ID sessionID) throws DeploymentException;
    
    public JSONArray getDataSources() throws JSONException;
    
    public JSONArray getDataConsumers() throws JSONException;
    
    public JSONArray getControllerAgents() throws JSONException;
}
