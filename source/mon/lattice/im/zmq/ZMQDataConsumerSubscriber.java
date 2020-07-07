package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 DataSources, DataConsumers, Probes and probes embeddedAttributes on the InfoPlane 
 using ZMQ.
**/

public class ZMQDataConsumerSubscriber extends AbstractZMQSubscriber implements Runnable {
    
    private final ZMQDataConsumerSubscriberHandler zmqDataConsumerSubscriberHandler = new ZMQDataConsumerSubscriberHandler();
    private Logger LOGGER = LoggerFactory.getLogger(ZMQDataConsumerSubscriber.class);
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQDataConsumerSubscriber(String remHost, int remPort, String filter) {
        super(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQDataConsumerSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	super(remHost, remPort, filter, context);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        zmqDataConsumerSubscriberHandler.messageDispatcher(message);
    }
    
    
    
    
    class ZMQDataConsumerSubscriberHandler extends ZMQAbstractSubscriberHandler {        
        
        @Override
        void parseDataSourceInfo(JSONObject msgObj) {
            try {
                String operation = msgObj.getString("operation");
                ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
                
                if (operation.equals("add")) {
                    dataSources.put(entityID, msgObj.getJSONObject("info"));
                }
                
                else if (operation.equals("remove")) {
                    JSONObject dataSource = dataSources.get(entityID);
                    
                    if (dataSource.has("probes")) {
                        JSONArray dsProbes = dataSource.getJSONArray("probes");
                        
                        for (int i=0; i<dsProbes.length(); i++) {
                            ID probeID = ID.fromString(dsProbes.getString(i));
                            probes.remove(probeID);
                            probeAttributes.remove(probeID);
                        }
                    }

                    dataSources.remove(entityID);
                        
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

                    // TODO: maybe change this to use an additional map instead of the one
                    // with the JSONObjects to avoid bottlenecks 
                    
                    ID dataSourceID = ID.fromString(probeInfo.getString("datasource"));
                    JSONObject dataSource = dataSources.get(dataSourceID);
                    
                    // this Data Source is not in the map
                    // this may happen if the Data Consumer was started afterwards
                    if (dataSource == null) {
                        dataSource = new JSONObject();
                        dataSources.put(dataSourceID, dataSource);
                    }
                    
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
                }
                else if (operation.equals("remove")) {
                    dataConsumers.remove(entityID);
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
                }
                else if (operation.equals("remove")) {
                    controllerAgents.remove(entityID);
                }

            } catch (JSONException e) {
                 LOGGER.error("Error while parsing Controller Agent information: " + e.getMessage());
            }
        }
    
    }
    
    
}