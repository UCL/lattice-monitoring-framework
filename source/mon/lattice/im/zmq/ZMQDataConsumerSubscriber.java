package mon.lattice.im.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
* An ZMQSubscriber is responsible for receiving information about  
* DataSources, DataConsumers, Probes and probes embeddedAttributes on the InfoPlane 
* using ZMQ.
**/

public class ZMQDataConsumerSubscriber extends AbstractZMQSubscriber implements Runnable {
    
    private final ZMQDataConsumerSubscriberHandler messageHandler = new ZMQDataConsumerSubscriberHandler(this);
    
    private Logger LOGGER = LoggerFactory.getLogger(ZMQDataConsumerSubscriber.class);
    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQDataConsumerSubscriber(String remHost, int remPort, String filter) {
        super(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQDataConsumerSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	super(remHost, remPort, filter, context);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        messageHandler.messageDispatcher(message);
    }
    
    
}