/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import java.io.IOException;
import java.io.Serializable;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.DataSource;
import mon.lattice.core.ID;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.Reporter;
import mon.lattice.core.plane.InfoPlane;
import mon.lattice.im.zmq.AbstractZMQInfoPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class ZMQControllerAgentInfoPlane extends AbstractZMQInfoPlane implements InfoPlane, ControllerAgentInteracter {
    
    // The hostname of the Subscriber.
    String remoteHost;

    // The port of the Subscriber
    int remotePort;
    
    ZMQControllerAgentPublisher zmqPublisher;
    
    ControllerAgent controllerAgent;
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQControllerAgentInfoPlane.class);
    

    public ZMQControllerAgentInfoPlane(String remoteHostname, int remotePort) {
	remoteHost = remoteHostname;
	this.remotePort = remotePort;

	zmqPublisher = new ZMQControllerAgentPublisher(remoteHost, this.remotePort);
    }
     
     
    /**
     * Connect to a delivery mechanism.
     */
    @Override
    public boolean connect() {
	return zmqPublisher.connect();
    }

    /**
     * Disconnect from a delivery mechanism.
     */
    @Override
    public boolean disconnect() {
	zmqPublisher.disconnect();
        zmqPublisher.destroyZMQContext();
        return true;
    }

    //@Override
    public String getInfoRootHostname() {
        return zmqPublisher.getRootHostname();
    }
    
    /**
     * Announce that the Data Source is up and running
     */
    @Override
    public boolean announce() {
        LOGGER.info("Announced this Controller Agent " + controllerAgent.getID());
        return addControllerAgentInfo(controllerAgent);
    }

    /**
     * Un-announce that the Data Source is up and running
     */
    @Override
    public boolean dennounce() {
        LOGGER.info("Deannounced this Controller Agent " + controllerAgent.getID());
        return removeControllerAgentInfo(controllerAgent);
    }

    @Override
    public ControllerAgent getControllerAgent() {
        return controllerAgent;
    }

    @Override
    public void setControllerAgent(ControllerAgent agent) {
        this.controllerAgent = agent;
    }

    @Override
    public boolean addControllerAgentInfo(ControllerAgent agent) {
        try {
            zmqPublisher.addControllerAgent(agent);
            LOGGER.info("just added Controller Agent " + agent);
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }

    @Override
    public boolean removeControllerAgentInfo(ControllerAgent mm) {
        try {
	    zmqPublisher.removeControllerAgent(mm);

	    LOGGER.info("just removed Controller Agent " + mm);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }
    

    @Override
    public boolean addDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean addProbeInfo(Probe p) {
        return false;
    }

    @Override
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        return false;
    }

    @Override
    public boolean modifyDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean modifyProbeInfo(Probe p) {
        return false;
    }

    @Override
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean removeDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean removeProbeInfo(Probe p) {
        return false;
    }

    @Override
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        return false;
    }
}
