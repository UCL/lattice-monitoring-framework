package mon.lattice.im.zmq;

import java.io.IOException;
import java.util.Collection;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQDataConsumerPublisher extends AbstractZMQPublisher {
    
    private Logger LOGGER = LoggerFactory.getLogger(ZMQDataConsumerPublisher.class);
    
    public ZMQDataConsumerPublisher(String remHost, int remPort) {
        super(remHost, remPort);
    }
    
    
    
    @Override
    public AbstractZMQPublisher addDataConsumer(ControllableDataConsumer dc) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject dataConsumerInfo = new JSONObject();
        try {
            dataConsumerInfo.put("id", dc.getID().toString());
            dataConsumerInfo.put("name", dc.getName());
            if (dc instanceof ControllableDataConsumer) {
                dataConsumerInfo.put("pid", ((ControllableDataConsumer) dc).getMyPID());
                JSONObject controlEndPoint = new JSONObject(dc.getControlPlane().getControlEndPoint());
                dataConsumerInfo.put("controlEndPoint", controlEndPoint);
            }
            infoObj.put("entity", "dataconsumer");
            infoObj.put("operation", "add"); // FIXME: could use an ENUM
            infoObj.put("info", dataConsumerInfo);
            LOGGER.debug(dataConsumerInfo.toString());
        } catch (JSONException e) {
            LOGGER.error("Error while formatting info" + e.getMessage());
        }
        sendInfo("info.dataconsumer", infoObj.toString());
        Collection<ControllableReporter> reporters = dc.getReportersCollection();
        for (ControllableReporter aReporter : reporters) {
            addReporter(aReporter);
        }
        return this;
    }
    
    
    @Override
    public AbstractZMQPublisher addReporter(ControllableReporter r) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject reporterInfo = new JSONObject();
            reporterInfo.put("id", r.getId().toString());
            reporterInfo.put("name", r.getName());
            reporterInfo.put("dataconsumer", r.getDcId().toString());
            infoObj.put("entity", "reporter");
            infoObj.put("operation", "add"); //FIXME
            infoObj.put("info", reporterInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        sendInfo("info.reporter", infoObj.toString());
        return this;
    }
    
    
    
    @Override
    public AbstractZMQPublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject dcInfo = new JSONObject();
            dcInfo.put("id", dc.getID().toString());
            infoObj.put("entity", "dataconsumer");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", dcInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        Collection<ControllableReporter> reporters = dc.getReportersCollection();
        for (ControllableReporter aReporter : reporters) {
            removeReporter(aReporter);
        }
        sendInfo("info.dataconsumer", infoObj.toString());
        return this;
    }
    
    
    @Override
    public AbstractZMQPublisher removeReporter(ControllableReporter r) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject reporterInfo = new JSONObject();
            reporterInfo.put("id", r.getId().toString());
            infoObj.put("entity", "reporter");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", reporterInfo);
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }
        sendInfo("info.reporter", infoObj.toString());
        return this;
    }

    
    
    @Override
    public AbstractZMQPublisher addDataSource(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher addProbe(Probe aProbe) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher addControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher removeDataSource(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher removeProbe(Probe aProbe) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }

    @Override
    public AbstractZMQPublisher removeControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported by a Data Consumer Publisher");
    }
    
    
    

    
}
