package mon.lattice.distribution.zmq;

/**
 *
 * @author uceeftu
 */
public class ZMQDataForwarderWithBind extends AbstractZMQDataForwarder {
  
    int localForwardingPort;
    
    public ZMQDataForwarderWithBind(int localPort, int localForwardingPort) {
        super();
        this.localPort = localPort;
        this.localForwardingPort = localForwardingPort;
    }

    
    
    @Override
    public boolean startProxy() {
        this.setName("zmq-data-forwarder");
        frontend.setLinger(0);
        backend.setLinger(0);
        frontend.bind("tcp://*:" + localPort);
        backend.bind("tcp://*:" + localForwardingPort);
        backend.bind(internalURI);
        this.start();
        return true;
    }
    
}
