/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import mon.lattice.management.ControllerAgentInfo;
import mon.lattice.management.DataConsumerInfo;
import mon.lattice.management.DataSourceInfo;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateProducer {
    void waitForAddedDataSource(DataSourceInfo dataSource, int timeout) throws InterruptedException;

    void waitForAddedDataConsumer(DataConsumerInfo dataConsumer, int timeout) throws InterruptedException;
    
    void waitForAddedControllerAgent(ControllerAgentInfo controllerAgent, int timeout) throws InterruptedException; 
    
    void waitForAddedProbe(ID probeID, int timeout) throws InterruptedException; 
    
    void waitForRemovedDataSource(DataSourceInfo dataSource, int timeout) throws InterruptedException;

    void waitForRemovedDataConsumer(DataConsumerInfo dataConsumer, int timeout) throws InterruptedException;
    
    void waitForRemovedControllerAgent(ControllerAgentInfo controllerAgent, int timeout) throws InterruptedException; 
    
    void waitForRemovedProbe(ID probeID, int timeout) throws InterruptedException; 
}
