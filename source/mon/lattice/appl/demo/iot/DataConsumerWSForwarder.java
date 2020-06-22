// DataConsumerWSForwarder.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: June 2020

package mon.lattice.appl.demo.iot;

import mon.lattice.core.AbstractDataConsumer;
import mon.lattice.core.PlaneInteracter;
import mon.lattice.core.MeasurementReceiver;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * A DataConsumerWSForwarder is a DataConsumer that then forwards the measurements
 * over a WebSocket.
 *
 * It uses a WebSocketReporter which is a Reporter specially defined to do the job.
 */
public class DataConsumerWSForwarder extends AbstractDataConsumer implements PlaneInteracter, MeasurementReceiver {
    // The WebSocketReporter
    WebSocketReporter reporter;
    
    /**
     * Construct a DataConsumerWSForwarder
     */
    public DataConsumerWSForwarder(String reporterName, InetSocketAddress dstAddr) throws IOException {
	// The default way to report a measurement is to print it
	reporter =  new WebSocketReporter(reporterName, dstAddr);
        
	addReporter(reporter);
    }

    public void startForwarding() throws IOException {
        reporter.connect();
    }

    public void stopForwarding() throws IOException {
        reporter.disconnect();
    }

}
