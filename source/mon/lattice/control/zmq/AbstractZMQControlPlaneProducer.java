/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.core.plane.ControllerControlPlane;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.control.im.ControlInformation;
import mon.lattice.control.im.ControlInformationInteracter;

/**
 * UDP based request-reply protocol to send control messages to Data Sources
 * connected to the control plane.
 * It also allows listeners to be added and called back when an announce message
 * is received from a Data Source on this plane (useful when the info plane 
 * implementation does not provide that functionality)
 * @author uceeftu
 */
public abstract class AbstractZMQControlPlaneProducer implements 
        ControllerControlPlane, ControlInformationInteracter  {
    
    ZMQRouter zmqRouter;
    int localControlPort;
    
    protected ControlInformation controlInformation;
    
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
    public ControlInformation getControlInformation() {
        return controlInformation;
    }

    @Override
    public void setControlInformation(ControlInformation im) {
        this.controlInformation = im;
        zmqRouter.setControlInformation(im);
    }
    
}
