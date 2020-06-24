/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import org.zeromq.ZContext;

/**
 *
 * @author uceeftu
 */
public final class ZMQXDRRequesterPool {
    int maxSize;
    LinkedBlockingQueue<ZMQXDRRequester> socketQueue;
    ZContext context;
    
    //private final static Logger LOGGER = LoggerFactory.getLogger(ZMQXDRRequesterPool.class);

    public ZMQXDRRequesterPool(int size, ZContext ctx) throws IOException {
        this.maxSize = size;
        this.socketQueue = new LinkedBlockingQueue(maxSize);
        this.context = ctx;
    }
    
    
    public void connect() throws IOException, InterruptedException {
        for (int i=0; i < maxSize; i++) {
            ZMQXDRRequester requester = new ZMQXDRRequester(context);
            requester.connect();
            socketQueue.put(requester);
        }
    }
    
    public void disconnect() throws IOException {
        for (ZMQXDRRequester t: socketQueue) {
            t.end();
        }
    }
    
    
    public ZMQXDRRequester getConnection() throws IOException, InterruptedException {
        return socketQueue.take();
    }
    
    public void releaseConnection(ZMQXDRRequester conn) throws InterruptedException {
        socketQueue.put(conn);
    }   
    
    
    
}
