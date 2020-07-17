/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.zmq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.core.ID;
import mon.lattice.im.AbstractIMNodeWithAnnounce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONObject;

/**
 * AbstractZMQIMNode contains the data structures utilised by the ZMQ 
 * Info Plane subscribers
 * 
 * @author uceeftu
 */
public abstract class AbstractZMQIMNode extends AbstractIMNodeWithAnnounce {
    
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQIMNode.class);
    
    Map<ID, JSONObject> dataSources;
    Map<ID, JSONObject> probes;
    Map<ID, JSONObject> probeAttributes;
    Map<ID, JSONObject> dataConsumers;
    Map<ID, JSONObject> reporters;
    Map<ID, JSONObject> controllerAgents;



    public AbstractZMQIMNode() {
        dataSources = new ConcurrentHashMap<>();
        probes = new ConcurrentHashMap<>();
        probeAttributes = new ConcurrentHashMap<>();
        dataConsumers = new ConcurrentHashMap<>();
        reporters = new ConcurrentHashMap<>();
        controllerAgents = new ConcurrentHashMap<>();
    }
    
    
    public void dump() {
        printDataSources();
        printProbes();
        printDataConsumers();
        printReporters();
    }
    
    
    public void printDataSources() {
        LOGGER.info(dataSources.toString());
    }
    
    public void printProbes() {
        LOGGER.info(probes.toString());
    }
    
    public void printAttributes() {
        LOGGER.info(probeAttributes.toString());
    }
    
    public void printDataConsumers() {
        LOGGER.info(dataConsumers.toString());
    }
    
    public void printReporters() {
        LOGGER.info(reporters.toString());
    }
    
    public void printControllerAgents() {
        LOGGER.info(controllerAgents.toString());
    }

    
    
}
