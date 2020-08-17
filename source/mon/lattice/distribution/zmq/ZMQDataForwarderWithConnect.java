package mon.lattice.distribution.zmq;

/**
 *
 * @author uceeftu
 */
public class ZMQDataForwarderWithConnect extends AbstractZMQDataForwarder {
    String forwardingUri;
    
    
    public ZMQDataForwarderWithConnect(int localPort, String remoteHost, int remotePort) {
        super();
        this.localPort = localPort;
        forwardingUri = "tcp://" + remoteHost + ":" + remotePort;
    }


    
    @Override
    public boolean startProxy() {
        this.setName("zmq-data-forwarder");
        frontend.setLinger(0);
        backend.setLinger(0);
        frontend.bind("tcp://*:" + localPort);
        backend.bind(internalURI);
        try {
            backend.connect(forwardingUri);
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            return false;
        }
        this.start();
        return true;
    }
}
