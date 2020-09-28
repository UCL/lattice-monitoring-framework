// UDPDataPlaneConsumerWithNames.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sep 2012

package mon.lattice.distribution.udp;

import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.distribution.Forwarding;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * This consumer also forwards every recevied Measurement.
 */
public class UDPDataPlaneForwardingConsumerWithNames extends UDPDataPlaneConsumerWithNames implements DataPlane, MeasurementReporting, Receiving, Forwarding, TransmittingData {

    // The address we are sending to
    InetSocketAddress transmitAddress;

    // The UDPTransmitter
    UDPTransmitter udpTransmitter;

    // A count
    int count = 0;

    /**
     * Construct a UDPDataPlaneForwardingConsumerWithNames.
     * @param addr the consumer address
     * @param tAddr the address to forward to, or null if no forwarding required
     */
    public UDPDataPlaneForwardingConsumerWithNames(InetSocketAddress addr, InetSocketAddress tAddr) {
        super(addr);
	// forwarding address
        transmitAddress = tAddr;
    }

    /**
     * Construct a UDPDataPlaneForwardingConsumerWithNames.
     * @param addr the consumer address
     */
    public UDPDataPlaneForwardingConsumerWithNames(InetSocketAddress addr) {
        super(addr);
    }

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
        boolean result = false;

        result = super.connect();

        if (result == true) {
            // consumer connected fine

            if (transmitAddress != null) {
                // connect the forwarder
                result = connectForwarder();
            }
        }

        return result;
    }


    /**
     * Connect the forwarder
     */
    public boolean connectForwarder() {
        // now try to connect to the transmitAddress
        // if forward address is specified
        if (transmitAddress != null) {
            try {
                // only connect if we're not already connected
                if (udpTransmitter == null) {
                    // Now connect to the IP address
                    UDPTransmitter tt = new UDPTransmitter(this, transmitAddress);

                    tt.connect();
		
                    udpTransmitter = tt;

                    return true;
                } else {
                    return true;
                }

            } catch (IOException ioe) {
                // Current implementation will be to do a stack trace
                //ioe.printStackTrace();

                return false;
            }

        } else {
            return false;
        }
    }

    /**
     * Disconnect from a delivery mechansim.
     */
    public boolean disconnect() {
        boolean result = false;

        result = super.disconnect();

        if (result == true) {
            // consumer connected fine

            if (udpTransmitter != null) {
                // connect the forwarder
                result = disconnectForwarder();
            }
        }

        return result;
    }

    /**
     * Disonnect the forwarder
     */
    public boolean disconnectForwarder() {
        // now try to disconnect the forwarder
        if (udpTransmitter != null) {
            try {
                udpTransmitter.end();
                udpTransmitter = null;
                return true;
            } catch (IOException ieo) {
                udpTransmitter = null;
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * Is it connected
     */
    public boolean isConnected() {
        return (udpReceiver == null ? false : true);
    }

    /**
     * Is forwarder connected
     */
    public boolean isForwarderConnected() {
        return (udpTransmitter == null ? false : true);
    }

    /**
     * Get the forwarder address
     */
    public InetSocketAddress getForwarderAddress() {
        return transmitAddress;
    }

    /**
     * Set up rge forwarder address
     */
    public void setForwarderAddress(InetSocketAddress tAddr) {
        // forwarder address
        transmitAddress = tAddr;
    }


    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular address.
     * The expected message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {
        bis.mark(0);

        // do usual received()
        super.received(bis, metaData);

        // forward IFF the transmitter is up and running
        if (udpTransmitter != null) {

            bis.reset();

            // now Transmit
            int volume = bis.available();

            // first convert a ByteArrayInputStream into a ByteArrayOutputStream
            ByteArrayOutputStream boas = new ByteArrayOutputStream(volume);


            // can we copy straight out of the ByteArrayInputStream
            if (bis instanceof ExposedByteArrayInputStream) {

                boas.write(((ExposedByteArrayInputStream)bis).toByteArray(), 0, volume);
            } else {
                // create temp buffer
                byte [] tmp = new byte[volume];
                // Reads up to len bytes of data into an array of bytes from this input stream.
                bis.read(tmp, 0, volume);

                // now copy to ByteArrayOutputStream
                boas.write(tmp, 0, volume);
            }
            
            udpTransmitter.transmit(boas, count); 

            count++;
        }
    }


    /**
     * Never called in the class.
     * Needed in order to implement Transmitting
     */
    public int transmit(DataPlaneMessage dsp) throws Exception {
        return 0;
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id) {
	return true;
    }


}

