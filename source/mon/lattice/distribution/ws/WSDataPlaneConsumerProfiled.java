package mon.lattice.distribution.ws;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.MetaData;
import org.slf4j.LoggerFactory;


public class WSDataPlaneConsumerProfiled extends WSDataPlaneConsumer {
    
    double mean = 0;
    long n = 0;

    public WSDataPlaneConsumerProfiled(InetSocketAddress addr) {
        super(addr);
    }

    public WSDataPlaneConsumerProfiled(int port) {
        super(port);
    }
    
    
    @Override
    public boolean disconnect() {
        System.err.println(mean);
        File logFile = new File("/tmp/" + "avg_decoding_time_" + System.currentTimeMillis() + ".log");
        try {
            FileWriter fw = new FileWriter(logFile);
            fw.write(String.valueOf((long)mean) + " ns");
            fw.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(WSDataPlaneConsumerProfiled.class).error("There was an error while writing the profiling stats");
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