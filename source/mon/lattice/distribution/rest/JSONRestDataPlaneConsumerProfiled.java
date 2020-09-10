package mon.lattice.distribution.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.MetaData;
import org.slf4j.LoggerFactory;


public class JSONRestDataPlaneConsumerProfiled extends JSONRestDataPlaneConsumer {
    
    double mean = 0;
    long n = 0;

    public JSONRestDataPlaneConsumerProfiled(int port, String endP) throws IOException {
        super(port, endP);
    }

    public JSONRestDataPlaneConsumerProfiled(int port, String endP, int threads) throws IOException {
        super(port, endP, threads);
    }

    public JSONRestDataPlaneConsumerProfiled(String remoteHost, int port, String endP) throws IOException {
        super(remoteHost, port, endP);
    }

    public JSONRestDataPlaneConsumerProfiled(String remoteHost, int port, String endP, int threads) throws IOException {
        super(remoteHost, port, endP, threads);
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
            LoggerFactory.getLogger(JSONRestDataPlaneConsumerProfiled.class).error("There was an error while writing the profiling stats");
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
