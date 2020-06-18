package mon.lattice.control.zmq;

import mon.lattice.distribution.Transmitting;
import mon.lattice.control.Transmitter;
import mon.lattice.control.SynchronousTransmitting;
import java.io.IOException;
import static mon.lattice.control.zmq.AbstractZMQControlPlaneProducer.LOGGER;
import mon.lattice.core.ID;
import org.zeromq.ZMQ;

/**
 * This is a UDP sender for monitoring messages
 */
public abstract class ZMQRequester implements SynchronousTransmitting, Transmitter {
    
    ZMQ.Context context;
    ZMQ.Socket sender;
    
    String identity;
    
    
    /**
    * Construct a sender from an existing ZMQ Context
    */
    
    public ZMQRequester(ZMQ.Context ctx) throws IOException {
        context = ctx;
        sender = context.socket(ZMQ.REQ);
        identity = ID.generate().toString();
        sender.setIdentity(identity.getBytes(ZMQ.CHARSET));
    }
    
    
    @Override
    public void setTransmitting(Transmitting transmitting) {

    }
        

    /**
     * Connect to the internal socket
     */
    @Override
    public void connect()  throws IOException {
        sender.setLinger(0);
        sender.setHWM(0);
	sender.connect("inproc://frontend");
    }

    /**
     * End the connection to the internal socket
     */
    @Override
    public void end() throws IOException {
	sender.close();
        context.term();
    }
    
    
    @Override
    public boolean transmitted(int id) {
        LOGGER.info("just transmitted Control Message with seqNo: " + id);
        return true;
    }
}
