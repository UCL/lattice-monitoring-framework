/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import mon.lattice.management.ControllerAgentInfo;
import mon.lattice.management.DataConsumerInfo;
import mon.lattice.management.DataSourceInfo;
import mon.lattice.management.Host;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateProducer {
    void addDataSource(DataSourceInfo dataSource, Host resource, int timeout) throws InterruptedException, DSNotFoundException;

    void addDataConsumer(DataConsumerInfo dataConsumer, Host resource, int timeout) throws InterruptedException, DCNotFoundException;
    
    void addControllerAgent(ControllerAgentInfo controllerAgent, Host resource, int timeout) throws InterruptedException, ControllerAgentNotFoundException; 
    
    void addProbe(ID probeID, int timeout) throws InterruptedException, ProbeNotFoundException; 
}
