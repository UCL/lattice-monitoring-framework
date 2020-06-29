package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.IMSubscriberNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 DataSources, DataConsumers, Probes and probes embeddedAttributes on the InfoPlane 
 using ZMQ.
**/

public class ZMQSubscriberWithAggregation extends AbstractZMQSubscriber implements IMSubscriberNode, Runnable {
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQSubscriberWithAggregation.class);
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQSubscriberWithAggregation(String remHost, int remPort, String filter) {
        super(remHost, remPort, filter, new ZContext(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZContext.
     */
    public ZMQSubscriberWithAggregation(String remHost, int remPort, String filter, ZContext context) {
	super(remHost, remPort, filter, context);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZContext.
     */
    public ZMQSubscriberWithAggregation(String internalURI, String filter, ZContext context) {
	super(internalURI, filter, context);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public ZMQSubscriberWithAggregation(int port, String filter) {
        super(port, filter);
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZContext.
     */
    
    public ZMQSubscriberWithAggregation(int port, String filter, ZContext context) {
	super(port, filter, context);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        JSONObject msgObj;
        try {
            msgObj = new JSONObject(message);
            ID entityID = null;
            String field = null;
            
            String entityType = msgObj.getString("entity");
            String operation = msgObj.getString("operation");
            
            if (!entityType.equals("probeattribute")) {
                entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
            }
            else
                field = msgObj.getJSONObject("info").getString("field");
            
            LOGGER.debug("Received: " + msgObj);
            
            
            switch(entityType) {
                case "datasource":  
                    if (operation.equals("add")) {
                        dataSources.put(entityID, msgObj.getJSONObject("info"));
                        sendMessageToListener(new AnnounceMessage(entityID, EntityType.DATASOURCE));
                    }
                    else if (operation.equals("remove")) {
                        dataSources.remove(entityID);
                        sendMessageToListener(new DeannounceMessage(entityID, EntityType.DATASOURCE));
                        
                        JSONArray embeddedProbes = msgObj.getJSONArray("probes");
                        for (int i=0; i < embeddedProbes.length(); i++)
                            messageHandler(embeddedProbes.getString(i));
                    }
                    
                    LOGGER.trace("datasource map:\n");
                    for (ID id: dataSources.keySet())
                        LOGGER.trace(dataSources.get(id).toString(1));
                    
                    break;
                        
                case "probe":
                    if (operation.equals("add")) {    
                        probes.put(entityID, msgObj.getJSONObject("info"));
                        sendMessageToListener(new AnnounceMessage(entityID, EntityType.PROBE));
                    }
                    else if (operation.equals("remove")) {
                        probes.remove(entityID);
                        sendMessageToListener(new DeannounceMessage(entityID, EntityType.PROBE));
                    }
                    
                    JSONArray embeddedAttributes = msgObj.getJSONArray("attributes");
                    for (int i=0; i < embeddedAttributes.length(); i++)
                        messageHandler(embeddedAttributes.getString(i));
                    
                    LOGGER.trace("probe map:\n");
                    for (ID id: probes.keySet())
                        LOGGER.trace(probes.get(id).toString(1));
                    
                    break;
                        
                case "probeattribute":
                    ID probeID = ID.fromString(msgObj.getJSONObject("info").getString("probe"));
                    JSONObject attributes;
                    
                    if (operation.equals("add")) {
                        
                        if (!probeAttributes.containsKey(probeID)) { 
                           attributes = new JSONObject();
                           attributes.put(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                           probeAttributes.put(probeID, attributes);
                        }
                        else {
                            attributes = probeAttributes.get(probeID);
                            attributes.accumulate(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                            probeAttributes.put(probeID, attributes);
                        }
                        
                    }
                    
                    else if (operation.equals("remove")) {
                        attributes = probeAttributes.get(probeID);
                        if (attributes == null)
                            break;
                        if (attributes.has(field))
                            attributes.remove(field);
                        if (!attributes.keys().hasNext())
                            probeAttributes.remove(probeID);
                    }
                    
                    LOGGER.trace("probeattribute map:\n");
                    for (ID id: probeAttributes.keySet())
                        LOGGER.trace(probeAttributes.get(id).toString(1));
                    
                    break;
                    
                        
                case "dataconsumer":  
                    if (operation.equals("add")) {
                        dataConsumers.put(entityID, msgObj.getJSONObject("info"));
                        sendMessageToListener(new AnnounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    else if (operation.equals("remove")) {
                        dataConsumers.remove(entityID);
                        sendMessageToListener(new DeannounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    break;
                        
                case "reporter":  
                    if (operation.equals("add")) {
                        reporters.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        reporters.remove(entityID);
                    }
                    
                    
                    LOGGER.debug("reporters map:\n");
                    for (ID id: reporters.keySet())
                        LOGGER.debug(reporters.get(id).toString(1));
                    
                    break;   
                    
                case "controlleragent":
                    if (operation.equals("add")) {
                        controllerAgents.put(entityID, msgObj.getJSONObject("info"));
                        sendMessageToListener(new AnnounceMessage(entityID, EntityType.CONTROLLERAGENT));
                    }
                    else if (operation.equals("remove")) {
                        controllerAgents.remove(entityID);
                        sendMessageToListener(new DeannounceMessage(entityID, EntityType.CONTROLLERAGENT));
                    }
            }
            
            
        } catch (JSONException e) {
            LOGGER.error("Error while deserializing received message" + e.getMessage());
        } 
    }
}
