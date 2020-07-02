/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    
    List<ID> sessions;
    List<ID> dataSources;
    List<ID> dataConsumers;
    List<ID> controllerAgents;
    
    
    public Host(String address, int port) {
        id = ID.generate();
        
        this.address = address;
        this.port = port;
        
        sessions = Collections.synchronizedList(new ArrayList());
        dataSources = Collections.synchronizedList(new ArrayList());
        dataConsumers = Collections.synchronizedList(new ArrayList());
        controllerAgents = Collections.synchronizedList(new ArrayList());
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
    
    public List<ID> getSessions() {
        return sessions;
    }
    
    
    public void addDataSource(ID dataSourceID) {
        dataSources.add(dataSourceID);
    }
    
    
    public void removeDataSource(ID dataSourceID) {
        dataSources.remove(dataSourceID);
    }

    
    public List<ID> getDataSources() {
        return dataSources;
    }
    
    
    public void addDataConsumer(ID dataConsumerID) {
        dataConsumers.add(dataConsumerID);
    }

    
    public void removeDataConsumer(ID dataConsumerID) {
        dataConsumers.remove(dataConsumerID);
    }
        
    public List<ID> getDataConsumers() {
        return dataConsumers;
    }
    
    
    public void addControllerAgent(ID dataConsumerID) {
        controllerAgents.add(dataConsumerID);
    }
    
    
    public void removeControllerAgent(ID controllerAgent) {
        controllerAgents.remove(controllerAgent);
    }

    
    public List<ID> getControllerAgents() {
        return controllerAgents;
    }
    
}
