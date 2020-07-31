package mon.lattice.im.zmq;

import java.io.IOException;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.im.AbstractIMNode;
import mon.lattice.im.IMPublisherNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractZMQPublisher extends AbstractIMNode implements IMPublisherNode {
    
    int remotePort;
    ZMQ.Context context;
    ZMQ.Socket publisherSocket;
    
    private Logger LOGGER = LoggerFactory.getLogger(AbstractZMQPublisher.class);

    
    public AbstractZMQPublisher(String remHost, int remPort) {
	remoteHost = remHost;
	remotePort = remPort;
        
        context = ZMQ.context(1);
        
        /* this should be a PUB. However there is an issue
           when a publisher "connects" to a subscriber as the first 
           published messages are lost. As our subscriber (controller) 
           does not really filter messages using PUSH does not make much difference
           and solves the above problem (no need to use unreliable sleep()).
        */
        publisherSocket = context.socket(SocketType.PUSH);
    }

    /**
     * Connect to the proxy Subscriber.
     */
    @Override
    public boolean connect() {
        String uri = "tcp://" + remoteHost + ":" + remotePort;
        publisherSocket.setLinger(5000);
        publisherSocket.setHWM(0);
        publisherSocket.connect(uri);
        return true;
    }

    /**
     * Disconnect from the DHT peers.
     */
    @Override
    public boolean disconnect() {
        publisherSocket.close();
        return true;
    }

    public void destroyZMQContext() {
        context.term();
    }

    public String getRootHostname() {
        return this.remoteHost;
    }

    public ZMQ.Context getContext() {
        return context;
    }
    
    
    /**
     * Send stuff to the Subscribers.
     */
    public boolean sendInfo(String aKey, String aValue) {
        LOGGER.debug("sending " + aKey + " => " + aValue);
        return publisherSocket.sendMore(aKey) && publisherSocket.send(aValue);
    }
    

    // add entities methods
    @Override
    public abstract AbstractZMQPublisher addDataSource(DataSource ds) throws IOException;
    
    @Override
    public abstract AbstractZMQPublisher addProbe(Probe aProbe) throws IOException;

    @Override
    public abstract AbstractZMQPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException;
    

    @Override
    public abstract AbstractZMQPublisher addDataConsumer(ControllableDataConsumer dc) throws IOException;
    
    @Override
    public abstract AbstractZMQPublisher addReporter(ControllableReporter r) throws IOException;
    

    @Override
    public abstract AbstractZMQPublisher addControllerAgent(ControllerAgent agent) throws IOException;
    
    
    // remove entities methods
    @Override
    public abstract AbstractZMQPublisher removeDataSource(DataSource ds) throws IOException;
    
    @Override
    public abstract AbstractZMQPublisher removeProbe(Probe aProbe) throws IOException;
    
    @Override
    public abstract AbstractZMQPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException;

    
    @Override
    public abstract AbstractZMQPublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException;
    
    @Override
    public abstract AbstractZMQPublisher removeReporter(ControllableReporter r) throws IOException;

    @Override
    public abstract AbstractZMQPublisher removeControllerAgent(ControllerAgent agent) throws IOException;
    
    
    

    @Override
    public AbstractZMQPublisher addDataConsumerInfo(ControllableDataConsumer dc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractZMQPublisher addDataSourceInfo(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractZMQPublisher modifyDataSource(DataSource ds) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractZMQPublisher modifyProbe(Probe p) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractZMQPublisher modifyProbeAttribute(Probe p, ProbeAttribute pa) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
