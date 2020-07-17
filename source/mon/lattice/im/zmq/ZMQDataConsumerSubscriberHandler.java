package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class ZMQDataConsumerSubscriberHandler extends ZMQAbstractSubscriberHandler { 
    
    private static Logger LOGGER = LoggerFactory.getLogger(ZMQControllerSubscriberHandler.class);
    AbstractZMQIMNode infoData;
    
    
    public ZMQDataConsumerSubscriberHandler(AbstractZMQIMNode infoData) {
        this.infoData = infoData;
    }
        
    @Override
    void parseDataSourceInfo(JSONObject msgObj) {
        try {
            String operation = msgObj.getString("operation");
            ID entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));

            if (operation.equals("add")) {
                infoData.dataSources.put(entityID, msgObj.getJSONObject("info"));
            }

            else if (operation.equals("remove")) {
                JSONObject dataSource = infoData.dataSources.get(entityID);

                if (dataSource.has("probes")) {
                    JSONArray dsProbes = dataSource.getJSONArray("probes");

                    for (int i=0; i<dsProbes.length(); i++) {
                        ID probeID = ID.fromString(dsProbes.getString(i));
                        infoData.probes.remove(probeID);
                        infoData.probeAttributes.remove(probeID);
                    }
                }

                infoData.dataSources.remove(entityID);

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
                infoData.probes.put(entityID, probeInfo);

                // TODO: maybe change this to use an additional map instead of the one
                // with the JSONObjects to avoid bottlenecks 

                ID dataSourceID = ID.fromString(probeInfo.getString("datasource"));
                JSONObject dataSource = infoData.dataSources.get(dataSourceID);

                // this Data Source is not in the map
                // this may happen if the Data Consumer was started afterwards
                if (dataSource == null) {
                    dataSource = new JSONObject();
                    infoData.dataSources.put(dataSourceID, dataSource);
                }

                dataSource.append("probes", entityID.toString());

                JSONArray embeddedAttributes = msgObj.getJSONArray("attributes");
                for (int i=0; i < embeddedAttributes.length(); i++) {
                    JSONObject embeddedAttributesAsJSON = new JSONObject(embeddedAttributes.getString(i));
                    parseProbeAttributeInfo(embeddedAttributesAsJSON);
                }
            }

            else if (operation.equals("remove")) {
                infoData.probes.remove(entityID);
                infoData.probeAttributes.remove(entityID);
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
                if (!infoData.probeAttributes.containsKey(probeID)) { 
                   attributes = new JSONObject();
                   attributes.put(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                   infoData.probeAttributes.put(probeID, attributes);
                }
                else {
                    attributes = infoData.probeAttributes.get(probeID);
                    attributes.accumulate(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                    infoData.probeAttributes.put(probeID, attributes);
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
               infoData.dataConsumers.put(entityID, msgObj.getJSONObject("info"));
            }
            else if (operation.equals("remove")) {
                infoData.dataConsumers.remove(entityID);
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
                infoData.reporters.put(entityID, msgObj.getJSONObject("info"));
            }
            else if (operation.equals("remove")) {
                infoData.reporters.remove(entityID);
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
                infoData.controllerAgents.put(entityID, msgObj.getJSONObject("info"));
            }
            else if (operation.equals("remove")) {
                infoData.controllerAgents.remove(entityID);
            }

        } catch (JSONException e) {
             LOGGER.error("Error while parsing Controller Agent information: " + e.getMessage());
        }
    }
    
}