package mon.lattice.im.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


public abstract class ZMQAbstractSubscriberHandler {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ZMQAbstractSubscriberHandler.class);
    
    abstract void parseDataSourceInfo(JSONObject msgObj);
    
    abstract void parseProbeInfo(JSONObject msgObj);
    
    abstract void parseProbeAttributeInfo(JSONObject msgObj);
    
    abstract void parseDataConsumerInfo(JSONObject msgObj);
    
    abstract void parseReporterInfo(JSONObject msgObj);
    
    abstract void parseControllerAgentInfo(JSONObject msgObj);
    
    
    public void messageDispatcher(String message) {
            JSONObject messageObject = null;
            String entityType = null;
            
            try {
                messageObject = new JSONObject(message);
                entityType = messageObject.getString("entity");
            } catch (JSONException e) {
                LOGGER.error("Error while deserializing received message" + e.getMessage());
            }
                        
            if (entityType != null && messageObject != null) switch(entityType) {
                case "datasource":
                    parseDataSourceInfo(messageObject);
                    break;
                case "probe":
                    parseProbeInfo(messageObject);
                    break;
                case "probeattribute":
                    parseProbeAttributeInfo(messageObject);
                    break;
                case "dataconsumer":
                    parseDataConsumerInfo(messageObject);
                    break;   
                case "reporter":
                    parseReporterInfo(messageObject);
                    break;
                case "controlleragent":
                    parseControllerAgentInfo(messageObject);
                    break;
            }
        
        }
    
}
