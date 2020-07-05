package mon.lattice.control.agents;

import java.io.IOException;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.im.zmq.AbstractZMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQControllerAgentPublisher extends AbstractZMQPublisher {
    
    private Logger LOGGER = LoggerFactory.getLogger(ZMQControllerAgentPublisher.class);
    
    
    public ZMQControllerAgentPublisher(String remHost, int remPort) {
        super(remHost, remPort);
    }
    
    
    @Override
    public AbstractZMQPublisher addControllerAgent(ControllerAgent agent) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject controllerAgentInfo = new JSONObject();
        try {
            controllerAgentInfo.put("id", agent.getID().toString());
            controllerAgentInfo.put("name", agent.getName());
            controllerAgentInfo.put("pid", agent.getPID());
            controllerAgentInfo.put("controlEndPoint", agent.getControlPlane().getControlEndPoint());
            infoObj.put("entity", "controlleragent");
            infoObj.put("operation", "add"); // FIXME: could use an ENUM
            infoObj.put("info", controllerAgentInfo);
            LOGGER.debug(controllerAgentInfo.toString());
        } catch (JSONException e) {
            LOGGER.error("Error while formatting info" + e.getMessage());
            throw new IOException(e.getMessage());
        }
        sendInfo("info.controlleragent", infoObj.toString());
        return this;
    }
    
    
    
    @Override
    public AbstractZMQPublisher removeControllerAgent(ControllerAgent agent) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject agentInfo = new JSONObject();
            agentInfo.put("id", agent.getID().toString());
            infoObj.put("entity", "controlleragent");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", agentInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        sendInfo("info.controlleragent", infoObj.toString());
        return this;
    }

    
    
    @Override
    public AbstractZMQPublisher addDataSource(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher addProbe(Probe aProbe) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher addDataConsumer(ControllableDataConsumer dc) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher addReporter(ControllableReporter r) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher removeDataSource(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher removeProbe(Probe aProbe) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }

    @Override
    public AbstractZMQPublisher removeReporter(ControllableReporter r) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Controller Agent Publisher");
    }
    
    
    
    
    
    
}
