package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.zmq.ZMQControllerSubscriber.ZMQControllerSubscriberHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 DataSources, DataConsumers, Probes and probes embeddedAttributes on the InfoPlane 
 using ZMQ.
**/

public class ZMQControllerSubscriber extends AbstractZMQSubscriber implements Runnable {
    
    private final ZMQControllerSubscriberHandler zmqControllerSubscriberHandler = new ZMQControllerSubscriberHandler();
    private Logger LOGGER = LoggerFactory.getLogger(ZMQControllerSubscriber.class);
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQControllerSubscriber(String remHost, int remPort, String filter) {
        super(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQControllerSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	super(remHost, remPort, filter, context);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQControllerSubscriber(String internalURI, String filter, ZMQ.Context context) {
	super(internalURI, filter, context);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public ZMQControllerSubscriber(int port, String filter) {
        super(port, filter);
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZMQ.Context.
     */
    
    public ZMQControllerSubscriber(int port, String filter, ZMQ.Context context) {
	super(port, filter, context);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        zmqControllerSubscriberHandler.messageDispatcher(message);
    }
    
    

    
    
    
    class ZMQControllerSubscriberHandler extends ZMQAbstractSubscriberHandler {
        
        @Override
        void parseDataSourceInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {
                    dataSources.put(entityID, msgObj.getJSONObject("info"));
                    sendMessageToListener(new AnnounceMessage(entityID, EntityType.DATASOURCE));
                }
                
                else if (operation.equals("remove")) {
                    JSONObject dataSource = dataSources.get(entityID);
                    
                    if (dataSource != null) {
                        if (dataSource.has("probes")) {
                            JSONArray dsProbes = dataSource.getJSONArray("probes");

                            for (int i=0; i<dsProbes.length(); i++) {
                                ID probeID = ID.fromString(dsProbes.getString(i));
                                probes.remove(probeID);
                                sendMessageToListener(new DeannounceMessage(probeID, EntityType.PROBE));
                                probeAttributes.remove(probeID);
                            }
                        }

                        dataSources.remove(entityID);
                        sendMessageToListener(new DeannounceMessage(entityID, EntityType.DATASOURCE));
                    }
                        
                }   
            } catch (JSONException e) {
                LOGGER.error("Error while parsing DataSource information: " + e.getMessage());
            }
        }
        
        
        @Override
        void parseProbeInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {    
                    JSONObject probeInfo = msgObj.getJSONObject("info");
                    probes.put(entityID, probeInfo);
                    sendMessageToListener(new AnnounceMessage(entityID, EntityType.PROBE));

                    // TODO: maybe change this to use an additional map instead of the one
                    // with the JSONObjects to avoid bottlenecks 
                    
                    ID dataSourceID = ID.fromString(probeInfo.getString("datasource"));
                    JSONObject dataSource = dataSources.get(dataSourceID);
                    dataSource.append("probes", entityID.toString());

                    JSONArray embeddedAttributes = msgObj.getJSONArray("attributes");
                    for (int i=0; i < embeddedAttributes.length(); i++) {
                        JSONObject embeddedAttributesAsJSON = new JSONObject(embeddedAttributes.getString(i));
                        parseProbeAttributeInfo(embeddedAttributesAsJSON);
                    }
                }

                else if (operation.equals("remove")) {
                    probes.remove(entityID);
                    probeAttributes.remove(entityID);
                    sendMessageToListener(new DeannounceMessage(entityID, EntityType.PROBE));       
                }
                
            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Probe information: " + e.getMessage());
            } 
        }
        
        @Override
        void parseProbeAttributeInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                String field = msgObj.getJSONObject("info").getString("field");
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
                    // Do something
                }
                
            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Probe Attribute information: " + e.getMessage());
            }
            
        }
        
        
        @Override
        void parseDataConsumerInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {
                   dataConsumers.put(entityID, msgObj.getJSONObject("info"));
                   sendMessageToListener(new AnnounceMessage(entityID, EntityType.DATACONSUMER));
                }
                else if (operation.equals("remove")) {
                    dataConsumers.remove(entityID);
                    sendMessageToListener(new DeannounceMessage(entityID, EntityType.DATACONSUMER));
                }
            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Data Consumer information: " + e.getMessage());
            }
        }
        
        
        @Override
        void parseReporterInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {
                    reporters.put(entityID, msgObj.getJSONObject("info"));
                }
                else if (operation.equals("remove")) {
                    reporters.remove(entityID);
                }

            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Reporter information: " + e.getMessage());
            }
        }
        
        
        
        @Override
        void parseControllerAgentInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {
                    controllerAgents.put(entityID, msgObj.getJSONObject("info"));
                    sendMessageToListener(new AnnounceMessage(entityID, EntityType.CONTROLLERAGENT));
                }
                else if (operation.equals("remove")) {
                    controllerAgents.remove(entityID);
                    sendMessageToListener(new DeannounceMessage(entityID, EntityType.CONTROLLERAGENT));
                }

            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Controller Agent information: " + e.getMessage());
            }
        }
    
    }
    
    
}
