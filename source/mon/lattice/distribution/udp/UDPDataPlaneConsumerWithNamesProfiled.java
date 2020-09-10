package mon.lattice.distribution.udp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.ws.WSDataPlaneConsumerProfiled;
import org.slf4j.LoggerFactory;


public class UDPDataPlaneConsumerWithNamesProfiled extends UDPDataPlaneConsumerWithNames {
    
    double mean = 0;
    long n = 0;

    public UDPDataPlaneConsumerWithNamesProfiled(InetSocketAddress addr) {
        super(addr);
    }

    public UDPDataPlaneConsumerWithNamesProfiled(int port) {
        super(port);
    }
    
    
    @Override
    public boolean disconnect() {
        System.err.println(mean);
        File logFile = new File("/tmp/" + "stats_" + System.currentTimeMillis() + ".log");
        try {
            FileWriter fw = new FileWriter(logFile);
            fw.write("avg_decoding_time " + String.valueOf((long)mean) + " ns\n");
            fw.write("received_measurements " + String.valueOf((long)n) + "\n");
            fw.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(UDPDataPlaneConsumerWithNamesProfiled.class).error("There was an error while writing the profiling stats");
        }
        
        return super.disconnect();
        
    }
    
    
    @Override
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        long tStart = System.nanoTime();
        super.received(bis, metaData);
        long tStop = System.nanoTime();
        
        long decodingTime = tStop - tStart;
        mean(decodingTime);
        
    }
    
    
    private void mean(long value) {
        mean += (value - mean) / ++n;
    }
    
}
