package mon.lattice.im.zmq;

import mon.lattice.control.im.ControlInformationManager;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.Reporter;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.control.im.ControlInformation;
import mon.lattice.control.im.ControlInformationInteracter;

/**
 * A ZMQControllerInfoPlane is an InfoPlane implementation
 that mainly collects data from the Information Model data.
 */
public class ZMQControllerInfoPlane extends AbstractZMQInfoPlane implements ControlInformationInteracter {
    private ControlInformation controlInformation;
    
    // The local port
    int port;

    ZMQProxy zmqProxy;
    
    /**
     * Constructor for subclasses.
     */
    private ZMQControllerInfoPlane() {
        setControlInformation(new ControlInformationManager(this));
        // setting the announce listener to the ControlInformation
    }


    /**
     * Construct a ZMQInfoPlaneConsumer.
     */
    public ZMQControllerInfoPlane(int localPort) {
        this();
	port = localPort;
        zmqProxy = new ZMQProxy(port);
        zmqSubscriber = new ZMQControllerSubscriber(zmqProxy.getInternalURI(), "info.", zmqProxy.getContext());
        zmqSubscriber.addAnnounceEventListener(controlInformation);
    }
    
    
    public ZMQControllerInfoPlane(int localPort, int workers) {
        this();
	port = localPort;
        zmqProxy = new ZMQProxy(port);
        zmqSubscriber = new ZMQControllerSubscriberWithWorkers(zmqProxy.getInternalURI(), "info.", zmqProxy.getContext(), workers);
        zmqSubscriber.addAnnounceEventListener(controlInformation);
    }
    
    
    /**
     * Connect to a delivery mechanism.
     */
    @Override
    public boolean connect() {
	return zmqProxy.startProxy() && zmqSubscriber.connect();
    }

    
    /**
     * Disconnect from a delivery mechanism.
     */
    @Override
    public boolean disconnect() {
	return zmqSubscriber.disconnect() && zmqProxy.stopProxy();
    }

    
    @Override
    public String getInfoRootHostname() {
        return zmqSubscriber.getRemoteHostname();
    }
   
    
   /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	return true;
    }

    
    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	return true;
    }

    
    /**
     * Consumer can never add a DataSource.
     * Return false
     */
    public boolean addDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never add a Probe.
     * Return false
     */
    public boolean addProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }
    
    @Override
    public boolean addControllerAgentInfo(ControllerAgent agent) {
        return false;
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        return false;
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        return false;
    }
    
    @Override
    public boolean removeControllerAgentInfo(ControllerAgent agent) {
        return false;
    }

    
    @Override
    public void setControlInformation(ControlInformation im) {
        this.controlInformation = im;
    }

    @Override
    public ControlInformation getControlInformation() {
        return this.controlInformation;
    }
    
}