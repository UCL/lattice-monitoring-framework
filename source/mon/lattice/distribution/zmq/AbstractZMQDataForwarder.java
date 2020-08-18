package mon.lattice.distribution.zmq;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractZMQDataForwarder extends Thread {
    
    ZMQ.Context context;
    ZMQ.Socket backend;
    ZMQ.Socket frontend;
    
    int localPort;
    
    String internalURI = "inproc://dataplane";
    

    public AbstractZMQDataForwarder() {
        context = ZMQ.context(1);
        backend = context.socket(SocketType.XPUB);
        frontend = context.socket(SocketType.XSUB);
    }

    
    public ZMQ.Context getContext() {
        return context;
    }

    
    public String getInternalURI() {
        return internalURI;
    }
    
    
    public abstract boolean startProxy();

    public boolean stopProxy() {
        frontend.close();
        backend.close();
        return true;
    }
    
    public boolean closeContext() {
        context.term();
        return true;
    }

    @Override
    public void run() {
        ZMQ.proxy(frontend, backend, null);
    }
    
}
