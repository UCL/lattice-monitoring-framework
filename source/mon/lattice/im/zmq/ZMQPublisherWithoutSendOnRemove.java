package mon.lattice.im.zmq;

import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import java.io.IOException;
import java.util.Collection;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;

/**
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQPublisherWithAggregation extends AbstractZMQPublisher {
    
    public ZMQPublisherWithAggregation(String remHost, int remPort) {
        super(remHost, remPort);
    }
    
    
    @Override
    public ZMQPublisherWithAggregation addProbe(Probe aProbe) throws IOException {
        DataSource ds = (DataSource) aProbe.getProbeManager();
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeInfo = new JSONObject();
            probeInfo.put("id", aProbe.getID().toString());
            probeInfo.put("name", aProbe.getName());
            probeInfo.put("datarate", aProbe.getDataRate().toString());
            probeInfo.put("on", aProbe.isOn());
            probeInfo.put("active", aProbe.isActive());
            probeInfo.put("datasource", ds.getID().toString());
            infoObj.put("entity", "probe");
            infoObj.put("operation", "add"); //FIXME
            infoObj.put("info", probeInfo);
            JSONArray attributes = new JSONArray();
            Collection<ProbeAttribute> attrs = aProbe.getAttributes();
            for (ProbeAttribute attr : attrs) {
                attributes.put(setAddProbeAttributeInfo(aProbe, attr));
            }
            infoObj.put("attributes", attributes);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        sendInfo("info.probe", infoObj.toString());
        return this;
    }
    

    /**
     * Add data for a ProbeAttribute.
     */
    @Override
    public ZMQPublisherWithAggregation addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        JSONObject infoObj = setAddProbeAttributeInfo(aProbe, attr);
        sendInfo("info.probeattribute", infoObj.toString());
        return this;
    }
    
    

    @Override
    public ZMQPublisherWithAggregation removeProbe(Probe aProbe) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeInfo = new JSONObject();
            probeInfo.put("id", aProbe.getID().toString());
            infoObj.put("entity", "probe");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", probeInfo);
            
            JSONArray attributes = new JSONArray();
            Collection<ProbeAttribute> attrs = aProbe.getAttributes();
            for (ProbeAttribute attr : attrs) {
                attributes.put(setRemoveProbeAttributeInfo(aProbe, attr));
            }
            infoObj.put("attributes", attributes);
            
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        
        sendInfo("info.probe", infoObj.toString());
        return this;
        
    }

    @Override
    public ZMQPublisherWithAggregation removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        JSONObject infoObj = setRemoveProbeAttributeInfo(aProbe, attr);
        sendInfo("info.probeattribute", infoObj.toString());
        return this;
    }
    
    
    private JSONObject setAddProbeAttributeInfo(Probe aProbe, ProbeAttribute attr) {
        JSONObject infoObj = new JSONObject();
        JSONObject attrInfo = new JSONObject();
        try {
            attrInfo.put("field", attr.getField());
            JSONObject attrProperties = new JSONObject();
            attrProperties.put("name", attr.getName());
            attrProperties.put("type", attr.getType().getCode());
            attrProperties.put("units", attr.getUnits());
            attrInfo.put("properties", attrProperties);
            attrInfo.put("probe", aProbe.getID().toString());
            infoObj.put("entity", "probeattribute");
            infoObj.put("operation", "add");
            infoObj.put("info", attrInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        return infoObj;
    }
        
    
    private JSONObject setRemoveProbeAttributeInfo(Probe aProbe, ProbeAttribute attr) {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeAttrsInfo = new JSONObject();
            probeAttrsInfo.put("probe", aProbe.getID().toString());
            probeAttrsInfo.put("field", attr.getField());
            infoObj.put("entity", "probeattribute");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", probeAttrsInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        return infoObj;
    }    
        
    
    
    

    
}
