/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public abstract class Host {
    protected ID id;
    
    protected String address;
    protected int port;
   
    protected File jarSource;
    protected File jarDestination;
    protected Long jarDeploymentDate;
    protected boolean jarDeployed;
    
    Set<ID> sessions;
    Set<ID> dataSources;
    Set<ID> dataConsumers;
    Set<ID> controllerAgents;
    
    
    public Host(String address, int port) {
        id = ID.generate();
        
        this.address = address;
        this.port = port;
        
        sessions = ConcurrentHashMap.newKeySet();
        dataSources = ConcurrentHashMap.newKeySet();
        dataConsumers = ConcurrentHashMap.newKeySet();
        controllerAgents = ConcurrentHashMap.newKeySet();
    }

    public ID getId() {
        return id;
    }
    
    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Long getJarDeploymentDate() {
        return jarDeploymentDate;
    }

    public void setJarDeploymentDate() {
        this.jarDeploymentDate = jarSource.lastModified();
    }
    
    public File getJarSource() {
        return jarSource;
    }
    
    public File getJarDestination() {
        return jarDestination;
    }
    
    public boolean isJarDeployed() {
        return jarDeployed;
    }

    public void setJarDeployed() {
        this.jarDeployed = true;
    }
    
    public void setJarDeploymentInfo(File source, File destination) {
        this.jarSource = source;
        this.jarDestination = destination;
    }

    
    public void addSession(ID sessionID) {
        sessions.add(sessionID);
    }
    
    public void removeSession(ID sessionID) {
        sessions.remove(sessionID);
    }
    
    public Set<ID> getSessions() {
        return sessions;
    }
    
    
    public void addDataSource(ID dataSourceID) {
        dataSources.add(dataSourceID);
    }
    
    
    public void removeDataSource(ID dataSourceID) {
        dataSources.remove(dataSourceID);
    }

    
    public Set<ID> getDataSources() {
        return dataSources;
    }
    
    
    public void addDataConsumer(ID dataConsumerID) {
        dataConsumers.add(dataConsumerID);
    }

    
    public void removeDataConsumer(ID dataConsumerID) {
        dataConsumers.remove(dataConsumerID);
    }
        
    public Set<ID> getDataConsumers() {
        return dataConsumers;
    }
    
    
    public void addControllerAgent(ID dataConsumerID) {
        controllerAgents.add(dataConsumerID);
    }
    
    
    public void removeControllerAgent(ID controllerAgent) {
        controllerAgents.remove(controllerAgent);
    }

    
    public Set<ID> getControllerAgents() {
        return controllerAgents;
    }
    
}
