/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management;

/**
 *
 * @author uceeftu
 */
public interface ManagementInterface<ReturnType> {
    
    ReturnType addUser(String username, String type, String token) throws Exception;
    
    ReturnType deleteUser(String userID) throws Exception;
    
    ReturnType createSession(String hostID, String userID) throws Exception;
    
    ReturnType deleteSession(String sessionID) throws Exception;
    
    ReturnType addHost(String address, String port) throws Exception;
    
    ReturnType removeHost(String hostID) throws Exception;
    
    ReturnType startDataSource(String className, String args, String sessionID) throws Exception;
    
    ReturnType stopDataSource(String dsID, String sessionID) throws Exception;
    
    ReturnType startDataConsumer(String className, String args, String sessionID) throws Exception;
    
    ReturnType stopDataConsumer(String dcID, String sessionID) throws Exception;
    
    ReturnType startControllerAgent(String className, String args, String sessionID) throws Exception;
    
    ReturnType stopControllerAgent(String caID, String sessionID) throws Exception;
    
    ReturnType getDataSources() throws Exception;
    
    ReturnType getDataConsumers() throws Exception;  
    
    ReturnType getControllerAgents() throws Exception;  
    
    public void init();
}
