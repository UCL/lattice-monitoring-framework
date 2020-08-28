// WSDataPlaneConsumerWithNames.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;

import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.DataPlaneMessageXDRDecoder;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * WSDataPlaneConsumer decodes Data Plane messages sent over WebSockets.
 * It extends WSDataPlaneConsumer and works with XDR encoded messages with names
 */
public class WSDataPlaneConsumerWithNames extends WSDataPlaneConsumer {

    /**
     * Construct a WSDataPlaneConsumerWithNames.
     */
    public WSDataPlaneConsumerWithNames(InetSocketAddress addr) {
        super(addr);
    }

    public WSDataPlaneConsumerWithNames(int port) {
        super(port);
    }
    
    @Override
    protected Measurement decodeMeasurement(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        DataPlaneMessageXDRDecoder decoder = new DataPlaneMessageXDRDecoder(getSeqNoMap());
        return decoder.decode(bis, metaData, true);
    }

}
