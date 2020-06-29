package mon.lattice.im.zmq;

import eu.fivegex.monitoring.appl.datasources.DockerDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import java.io.IOException;
import java.util.Collection;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ControllableReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;

/**
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQDataSourcePublisher extends AbstractZMQPublisher {
    
    private Logger LOGGER = LoggerFactory.getLogger(ZMQDataSourcePublisher.class);
    
    public ZMQDataSourcePublisher(String remHost, int remPort) {
        super(remHost, remPort);
    }
    
    
    
    @Override
    public ZMQDataSourcePublisher addDataSource(DataSource ds) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject dataSourceInfo = new JSONObject();
        try {
            dataSourceInfo.put("id", ds.getID().toString());
            dataSourceInfo.put("name", ds.getName());
            JSONObject controlEndPoint;
            if (ds instanceof DockerDataSource && ((DockerDataSource) ds).getDataSourceConfigurator() != null) {
                LOGGER.debug("A Docker Data Source instance");
                String externalHost = ((DockerDataSource) ds).getDataSourceConfigurator().getDockerHost();
                int controlPort = ((DockerDataSource) ds).getDataSourceConfigurator().getControlForwardedPort();
                controlEndPoint = new JSONObject();
                controlEndPoint.put("address", externalHost);
                controlEndPoint.put("port", controlPort);
                controlEndPoint.put("type", "socket/NAT");
                dataSourceInfo.put("controlEndPoint", controlEndPoint);
            } else if (ds instanceof ControllableDataSource) {
                dataSourceInfo.put("pid", ((ControllableDataSource) ds).getMyPID());
                dataSourceInfo.put("controlEndPoint", ds.getControlPlane().getControlEndPoint());
            }
            infoObj.put("entity", "datasource");
            infoObj.put("operation", "add"); // FIXME: could use an ENUM
            infoObj.put("info", dataSourceInfo);
            LOGGER.debug(dataSourceInfo.toString());
        } catch (JSONException e) {
            LOGGER.error("Error while formatting info" + e.getMessage());
            throw new IOException(e.getMessage());
        }
        sendInfo("info.datasource", infoObj.toString());
        Collection<Probe> probes = ds.getProbes();
        for (Probe aProbe : probes) {
            addProbe(aProbe);
        }
        return this;
    }
    
    
    @Override
    public ZMQDataSourcePublisher addProbe(Probe aProbe) throws IOException {
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
    


    @Override
    public ZMQDataSourcePublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        JSONObject infoObj = setAddProbeAttributeInfo(aProbe, attr);
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
    
    
    
    @Override
    public ZMQDataSourcePublisher removeDataSource(DataSource ds) throws IOException {
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
        sendInfo("info.datasource", infoObj.toString());
        return this;
    }
    
    

    @Override
    public ZMQDataSourcePublisher removeProbe(Probe aProbe) throws IOException {
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
        sendInfo("info.probe", infoObj.toString());
        return this;
        
    }

    @Override
    public ZMQDataSourcePublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
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

    
    @Override
    public ZMQDataSourcePublisher addDataConsumer(ControllableDataConsumer dc) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }

    @Override
    public ZMQDataSourcePublisher addReporter(ControllableReporter r) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }

    @Override
    public ZMQDataSourcePublisher addControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }

    @Override
    public ZMQDataSourcePublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }

    @Override
    public ZMQDataSourcePublisher removeReporter(ControllableReporter r) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }

    @Override
    public ZMQDataSourcePublisher removeControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Source Publisher");
    }
    
    
    
    
    
    
    

    
}
