package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.im.AbstractIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.IMSubscriberNode;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 * DataSources, DataConsumers, Probes and probes attributes on the InfoPlane 
 * using ZMQ.
**/

public abstract class AbstractZMQSubscriber extends AbstractIMNode implements IMSubscriberNode, Runnable {
    int remotePort = 0;
    int localPort = 0;
    
    ZContext context;
    ZMQ.Socket subscriberSocket;
    
    String internalURI;
    String messageFilter;
    
    boolean threadRunning = false;
    
    Map<ID, JSONObject> dataSources = new ConcurrentHashMap<>();
    Map<ID, JSONObject> probes = new ConcurrentHashMap<>();
    Map<ID, JSONObject> probeAttributes = new ConcurrentHashMap<>();
    
    Map<ID, JSONObject> dataConsumers = new ConcurrentHashMap<>();
    Map<ID, JSONObject> reporters = new ConcurrentHashMap<>();
    
    Map<ID, JSONObject> controllerAgents = new ConcurrentHashMap<>();
    
    AnnounceEventListener listener;
    
    Thread thread = new Thread(this, "zmq-info-subscriber");
    
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQSubscriber.class);

    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public AbstractZMQSubscriber(String remHost, int remPort, String filter) {
        this(remHost, remPort, filter, new ZContext(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZContext.
     */
    public AbstractZMQSubscriber(String remHost, int remPort, String filter, ZContext context) {
	remoteHost = remHost;
	remotePort = remPort;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = context.createSocket(SocketType.SUB);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZContext.
     */
    public AbstractZMQSubscriber(String internalURI, String filter, ZContext context) {
	this.internalURI = internalURI;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = context.createSocket(SocketType.SUB);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public AbstractZMQSubscriber(int port, String filter) {
        this(port, filter, new ZContext(1));
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZContext.
     */
    
    public AbstractZMQSubscriber(int port, String filter, ZContext context) {
	localPort = port;
        messageFilter = filter; 
        
        this.context = context;
        subscriberSocket = context.createSocket(SocketType.SUB);
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connectAndListen() {
        String uri;
        if (remoteHost != null && remotePort != 0)
            uri = "tcp://" + remoteHost + ":" + remotePort;
        else {
            uri = internalURI;
            // sleeping before connecting to the inproc socket
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
        subscriberSocket.setRcvHWM(0);
        subscriberSocket.connect(uri);
        thread.start();
        return true;
    }
    
    
    @Override
    public boolean connect() {
        return this.connectAndListen();
    }
    
    
    
    public boolean bindAndListen() {
        subscriberSocket.bind("tcp://*:" + localPort);
        thread.start();
        return true;
    }

    
    public ZMQ.Socket getSubscriberSocket() {
        return subscriberSocket;
    }
    

    /**
     * Disconnect from the DHT peers.
     */
    @Override
    public boolean disconnect() {
        threadRunning = false;
        return true;
    }

    @Override
    public String getRemoteHostname() {
        return this.remoteHost;
    }
    
    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeOut) {
        return dataSources.containsKey(dataSourceID);
    }
    

    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut) {
        return dataConsumers.containsKey(dataConsumerID);
    }
    
    
    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeOut) {
        return controllerAgents.containsKey(controllerAgentID);
    }
    
    @Override
    public boolean containsProbe(ID probeID, int timeOut) {
        return probes.containsKey(probeID);
    }
    
    
    @Override
    public Object getDataSourceInfo(ID dataSourceID, String info) {
        try {
            JSONObject dataSource = dataSources.get(dataSourceID);
            Object dataSourceInfo = dataSource.get(info);
            return dataSourceInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Data Source info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getProbeInfo(ID probeID, String info) {
        try {
            JSONObject probe = probes.get(probeID);
            Object probeInfo = probe.get(info);
            return probeInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    
    @Override
    public Object getProbeAttributeInfo(ID probeID, Integer field, String info) {
        try {
            JSONObject probeAttribute = probeAttributes.get(probeID);
            Object probeAttributeInfo = probeAttribute.getJSONObject(field.toString()).get(info);
            return probeAttributeInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Attribute info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getDataConsumerInfo(ID dataConsumerID, String info) {
        try {
            JSONObject dataConsumer = dataConsumers.get(dataConsumerID);
            Object dataConsumerInfo = dataConsumer.get(info);
            return dataConsumerInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Data Consumer info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    //@Override
    public Object getControllerAgentInfo(ID controllerAgentID, String info) {
        try {
            JSONObject controllerAgent = controllerAgents.get(controllerAgentID);
            Object controllerAgentInfo = controllerAgent.get(info);
            return controllerAgentInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Controller Agent info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    
    @Override
    public Object getReporterInfo(ID reporterID, String info) {
        try {
            JSONObject reporter = reporters.get(reporterID);
            Object reporterInfo = reporter.get(info);
            return reporterInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getProbesOnDataSource(ID dataSourceID) {
        JSONArray probesOnDS = new JSONArray();
        try {
            for (ID probeID : probes.keySet()) {
                if (probes.get(probeID).get("datasource").equals(dataSourceID.toString()))
                    probesOnDS.put(probeID.toString());
                }
                    
            } catch (JSONException e) {
                LOGGER.error("Error while retrieving Probe info" + e.getMessage());
                return null;
            }
        return probesOnDS;       
    }
    
    
    @Override
    public void run() {
        subscriberSocket.subscribe(messageFilter.getBytes());
        
        LOGGER.info("Listening for messages");
        
        threadRunning = true;
        try {
            while (threadRunning) {
                String header = subscriberSocket.recvStr();
                String content = subscriberSocket.recvStr();
                LOGGER.debug(header + " : " + content);
                messageHandler(content);
            }
            } catch (ZMQException e) {
                subscriberSocket.close();
                LOGGER.debug(e.getMessage());
            }
        subscriberSocket.close();
        context.destroy();
    }
    
    
    abstract protected void messageHandler(String message);
    
    
    @Override
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    @Override
    public void sendMessage(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
}
