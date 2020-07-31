package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.IMSubscriberNode;
import org.zeromq.SocketType;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 * DataSources, DataConsumers, Probes and probes attributes on the InfoPlane 
 * using ZMQ.
**/

public abstract class AbstractZMQSubscriber extends AbstractZMQIMNode implements IMSubscriberNode, Runnable {
    int remotePort = 0;
    int localPort = 0;
    
    ZMQ.Context context;
    ZMQ.Socket subscriberSocket;
    
    String internalURI;
    String messageFilter;
    
    boolean threadRunning = false;
    
    Thread thread = new Thread(this, "zmq-info-subscriber");
    
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQSubscriber.class);
    
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public AbstractZMQSubscriber(String remHost, int remPort, String filter) {
        this(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public AbstractZMQSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	remoteHost = remHost;
	remotePort = remPort;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = context.socket(SocketType.SUB);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public AbstractZMQSubscriber(String internalURI, String filter, ZMQ.Context context) {
	this.internalURI = internalURI;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = context.socket(SocketType.SUB);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public AbstractZMQSubscriber(int port, String filter) {
        this(port, filter, ZMQ.context(1));
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZMQ.Context.
     */
    
    public AbstractZMQSubscriber(int port, String filter, ZMQ.Context context) {
	localPort = port;
        messageFilter = filter; 
        
        this.context = context;
        subscriberSocket = context.socket(SocketType.SUB);
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connectAndListen() {
        String uri;
        
        if (remoteHost != null && remotePort != 0)
            uri = "tcp://" + remoteHost + ":" + remotePort;
        else
            uri = internalURI;
        
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
    

    @Override
    public boolean disconnect() {
        threadRunning = false;
        return true;
    }
    
    
    abstract protected void messageHandler(String message);
     
    
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
        context.term();
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
    
    @Override
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
        JSONObject dataSourceInfo = this.dataSources.get(dataSourceID);
        try {
            return dataSourceInfo.getJSONArray("probes");
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
    
}
