package mon.lattice.im.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 *
 * @author uceeftu
 */
public class ZMQSubscriberWorker implements Runnable {
    
    ZMQ.Context context;
    ZMQ.Socket pullSocket;
    
    String dispatcherURI;
    
    ZMQControllerSubscriberHandler messageHandler;
    
    private static Logger LOGGER = LoggerFactory.getLogger(ZMQSubscriberWorker.class);
    
    
    public ZMQSubscriberWorker(AbstractZMQIMNode infoData, String uri, ZMQ.Context c) {
        dispatcherURI = uri;
        context = c;
        pullSocket = context.socket(SocketType.PULL);
        messageHandler = new ZMQControllerSubscriberHandler(infoData);
    }
    
    
    public boolean connect() {
        LOGGER.debug(Thread.currentThread().getName() + " Connected!");
        return pullSocket.connect(dispatcherURI);
    }
    
    
    @Override
    public void run() {
        LOGGER.info(Thread.currentThread().getName() + " Started!");
        connect();
        while (!Thread.interrupted()) {
            try {
                String header = pullSocket.recvStr();
                String content = pullSocket.recvStr();
                LOGGER.debug(header + " : " + content);
                messageHandler.messageDispatcher(content);
            } catch (ZMQException e) {
                pullSocket.close();
                LOGGER.debug(e.getMessage());
            }
        }
        LOGGER.info(Thread.currentThread().getName() + " Terminated!");
        pullSocket.close();
        context.term();
    }   
    

    
}
