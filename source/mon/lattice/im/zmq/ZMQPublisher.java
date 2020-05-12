package mon.lattice.im.zmq;

import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import java.io.IOException;
import java.util.Collection;
import mon.lattice.im.IMPublisherNode;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQPublisher extends AbstractZMQPublisher implements IMPublisherNode {
    
    public ZMQPublisher(String remHost, int remPort) {
	super(remHost, remPort);
    }
    
    
    /**
     * Add data for a Probe.
     */
    @Override
    public ZMQPublisher addProbe(Probe aProbe) throws IOException {
        DataSource ds = (DataSource)aProbe.getProbeManager();
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
            
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }

        sendInfo("info.probe", infoObj.toString());
        
        Collection<ProbeAttribute> attrs = aProbe.getAttributes();
        for (ProbeAttribute attr : attrs) {
	    addProbeAttribute(aProbe, attr);
	}
        
	return this;
    }

    /**
     * Add data for a ProbeAttribute.
     */
    @Override
    public ZMQPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
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
        
        sendInfo("info.probeattribute", infoObj.toString());
        
	return this;
    }
    
    
    @Override
    public AbstractZMQPublisher removeDataSource(DataSource ds) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject dsInfo = new JSONObject();
            dsInfo.put("id", ds.getID().toString());
            infoObj.put("entity", "datasource");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", dsInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        Collection<Probe> probes = ds.getProbes();
        for (Probe aProbe : probes) {
            removeProbe(aProbe);
        }
        sendInfo("info.datasource", infoObj.toString());
        return this;
    }
    
    
    
    @Override
    public ZMQPublisher removeProbe(Probe aProbe) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeInfo = new JSONObject();
            probeInfo.put("id", aProbe.getID().toString());
            infoObj.put("entity", "probe");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", probeInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        Collection<ProbeAttribute> attrs = aProbe.getAttributes();
        for (ProbeAttribute attr : attrs) {
            removeProbeAttribute(aProbe, attr);
        }
        sendInfo("info.probe", infoObj.toString());
        return this;
    }
    

    @Override
    public ZMQPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
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
        sendInfo("info.probeattribute", infoObj.toString());
        return this;
    }
    
}
