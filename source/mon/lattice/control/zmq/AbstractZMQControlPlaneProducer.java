/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.im.delegate.InfoPlaneDelegate;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.core.plane.ControllerControlPlane;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP based request-reply protocol to send control messages to Data Sources
 * connected to the control plane.
 * It also allows listeners to be added and called back when an announce message
 * is received from a Data Source on this plane (useful when the info plane 
 * implementation does not provide that functionality)
 * @author uceeftu
 */
public abstract class AbstractZMQControlPlaneProducer implements 
        ControllerControlPlane, /*SynchronousTransmitting,*/ InfoPlaneDelegateInteracter  {
    
    ZMQRouter zmqRouter;
    int localControlPort;
    
    protected InfoPlaneDelegate infoPlaneDelegate;
    
    static Logger LOGGER = LoggerFactory.getLogger("ZMQControlPlaneProducer");
    
    
    
    
    public AbstractZMQControlPlaneProducer(int port) {
        this.localControlPort = port;
        this.zmqRouter = new ZMQRouter(localControlPort);
    }
    

    @Override
    public boolean connect() {
	zmqRouter.bind();
        return true;
    }

    @Override
    public boolean disconnect() {
        zmqRouter.disconnect();
	return true;
    }

    
    @Override
    public boolean announce() {
        // sending announce messages is not expected for a Control Plane Producer
	return false;
    }

    @Override
    public boolean dennounce() {
        // sending deannounce messages is not expected for a Control Plane Producer
	return false;
    }
    
    
    @Override
    public Map getControlEndPoint() {
        throw new UnsupportedOperationException("Getting control endpoint is not supported on a producer");
    }
    

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return infoPlaneDelegate;
    }

    @Override
    public void setInfoPlaneDelegate(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }
    
}
