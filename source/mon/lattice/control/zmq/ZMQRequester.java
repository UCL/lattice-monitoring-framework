package mon.lattice.control.zmq;

import mon.lattice.distribution.Transmitting;
import mon.lattice.control.Transmitter;
import mon.lattice.control.SynchronousTransmitting;
import java.io.IOException;
import static mon.lattice.control.zmq.AbstractZMQControlPlaneProducer.LOGGER;
import org.zeromq.ZMQ;


/**
 * This is a UDP sender for monitoring messages
 */
public abstract class ZMQRequester implements SynchronousTransmitting, Transmitter {
    
    ZMQ.Context context;
    
    
    /**
    * Construct a sender from an existing ZMQ Context
    */
    
    public ZMQRequester(ZMQ.Context ctx) {
        context = ctx;
    }
    
    
    @Override
    public void setTransmitting(Transmitting transmitting) {

    }
        

    /**
     * Connect to the internal socket
     */
    @Override
    public void connect() {

    }

    /**
     * End the connection to the internal socket
     */
    @Override
    public void end() throws IOException {
        context.term();
    }
    
    
    @Override
    public boolean transmitted(int id) {
        LOGGER.debug("just transmitted Control Message with seqNo: " + id);
        return true;
    }
    
    
}
