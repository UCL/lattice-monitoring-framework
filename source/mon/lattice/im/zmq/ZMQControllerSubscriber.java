package mon.lattice.im.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 DataSources, DataConsumers, Probes and probes embeddedAttributes on the InfoPlane 
 using ZMQ.
**/

public class ZMQControllerSubscriber extends AbstractZMQSubscriber implements Runnable {
    
    ZMQControllerSubscriberHandler messageHandler = new ZMQControllerSubscriberHandler(this);
    
    private Logger LOGGER = LoggerFactory.getLogger(ZMQControllerSubscriber.class);
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQControllerSubscriber(String remHost, int remPort, String filter) {
        super(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQControllerSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	super(remHost, remPort, filter, context);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQControllerSubscriber(String internalURI, String filter, ZMQ.Context context) {
	super(internalURI, filter, context);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public ZMQControllerSubscriber(int port, String filter) {
        super(port, filter);
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZMQ.Context.
     */
    
    public ZMQControllerSubscriber(int port, String filter, ZMQ.Context context) {
	super(port, filter, context);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        messageHandler.messageDispatcher(message);
    }
    
    
}
