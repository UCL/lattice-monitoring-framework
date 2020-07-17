package mon.lattice.im.zmq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 * A ZMQControllerSubscriberWithWorkers is responsible for receiving information 
 * about Lattice entities being started via the InfoPlane using ZMQ. It has
 * a number of workers to process the incoming messages.
**/

public class ZMQControllerSubscriberWithWorkers extends AbstractZMQSubscriber implements Runnable {
    
    ZMQ.Socket dispatcherSocket;
    String dispatcherURI;
    ExecutorService executor;
    int workers;
    
    private static Logger LOGGER = LoggerFactory.getLogger(ZMQControllerSubscriberWithWorkers.class);
    
    
    /**
     * Construct a ZMQControllerSubscriberWithWorkers given an internalURI, a remote 
     * a message filter and an existing ZMQ.Context.
     * 
     * @param internalURI - this is the internal uri where the subscriber socket is bound to
     * @param workers - the number of workers processing incoming messages
     */
    public ZMQControllerSubscriberWithWorkers(String internalURI, String filter, ZMQ.Context context, int workers) {
	super(internalURI, filter, context);
        dispatcherSocket = context.socket(SocketType.PUSH);
        dispatcherURI = "inproc://dispatcher";
        this.workers = workers;
        executor = Executors.newFixedThreadPool(this.workers);
    }
    
    

    @Override
    public boolean connect() {
        return super.connect() && startWorkers();
    }
    
    
    @Override
    public boolean disconnect() {
        return super.disconnect() && stopWorkers();
    }
    
    
    private boolean startWorkers() {
        for (int i=0; i < workers; i++) {
            ZMQSubscriberWorker worker = new ZMQSubscriberWorker(this, dispatcherURI, context);
            executor.submit(worker);
        }
        return true;
    }
    
    
    private boolean stopWorkers() {
        executor.shutdown();
        return true;
    }
    
    
    @Override
    public void run() {
        subscriberSocket.subscribe(messageFilter.getBytes());
        dispatcherSocket.bind(dispatcherURI);
        LOGGER.info("Listening for messages");
        ZMQ.proxy(subscriberSocket, dispatcherSocket, null);
    }
    
    
    
    @Override
    protected void messageHandler(String message) {
        // the handling is delegated to the workers
    }
    
    
}
