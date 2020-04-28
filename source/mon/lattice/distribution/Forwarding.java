// Transmitting.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2019

package mon.lattice.distribution;

import mon.lattice.core.TypeException;
import mon.lattice.core.plane.DataPlaneMessage;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * An interface for distribution components that need
 * to do Forwarding of DataPlaneMessage objects.
 * They do transmission usually of received data.
 */
public interface Forwarding extends Transmitting {
    /**
     * Connect the forwarder
     */
    public boolean connectForwarder();

    /**
     * Disonnect the forwarder
     */
    public boolean disconnectForwarder();

    /**
     * Is forwarder connected
     */
    public boolean isForwarderConnected();

    /**
     * Get the forwarder address
     */
    public InetSocketAddress getForwarderAddress();

    /**
     * Set up rge forwarder address
     */
    public void setForwarderAddress(InetSocketAddress tAddr);
    
}
