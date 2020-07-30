//author: Alina and Francesco

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Timestamp;
import mon.lattice.core.ProbeValueWithName;
import us.monoid.web.Resty;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Content;
import us.monoid.web.Resty.Option;

/**
 * A BufferReporter groups and sends the Measurements to a specific function.
 */
@Deprecated
public class BufferedRestReporter extends AbstractControllableReporter {
    /**
     * In a BufferReporter, report() groups and sends the Measurement to a specific function.
     */
    Integer bufferSize;
    String uri;
    
    BlockingQueue<Measurement> queue;
    Thread worker;
    
    int maxQueueLength;
    boolean fullQueue = false;

    Resty resty = new Resty(Option.timeout(0));
    JSONArray array = new JSONArray();
    
    private Logger LOGGER = LoggerFactory.getLogger(BufferedRestReporter.class);


    
        
    public BufferedRestReporter(String reporterName, String bufferSize, String ip, String port, String method) {
        this(reporterName, bufferSize, ip, port, method, "1000000"); // default max size 1M
    }      
    
    
    public BufferedRestReporter(String reporterName, String bufferSize, String ip, String port, String method, String maxQueueLength) {
        super(reporterName); 
        this.bufferSize = Integer.valueOf(bufferSize);
        this.uri = "http://" + ip + ":" + port + method;
        this.maxQueueLength = Integer.valueOf(maxQueueLength);
        this.queue = new LinkedBlockingQueue(this.maxQueueLength);
    }
    
    
    @Override
    public void init() throws Exception {
        worker = new Thread(() -> this.dequeue(), getName() + "-worker-thread");
        worker.start();
    }
    
    
    @Override
    public void cleanup() throws Exception {
        worker.interrupt();
    }
    
    
    @Override
    public void report(Measurement m) {
	LOGGER.debug("Received measurement: " + m.toString());
        
        if (!queue.offer(m) && !fullQueue) {
            LOGGER.error("*** Queue is full! ***");
            fullQueue = true;
        }
    }
    
    
    private JSONObject processMeasurement(Measurement m) {
        Timestamp t = m.getTimestamp();
        JSONObject obj = new JSONObject();

        for (ProbeValue attribute : m.getValues()) {
            try {
                obj.put("id", m.getServiceID().toString());
                obj.put("probeid", m.getProbeID().toString());
                obj.put("timestamp", t.toString());
                obj.put("type", (((ProbeValueWithName)attribute).getName()));
                obj.put("value", attribute.getValue());
                }
            catch (JSONException e) {
                for (StackTraceElement stackTrace : e.getStackTrace()) {
                    LOGGER.error(stackTrace.toString());
                }
            }
        }    
        return obj;
    }
    
    
    private void sendRequest() throws IOException, JSONException {
        LOGGER.debug(array.toString());
        Content payload = new Content("application/json", array.toString().getBytes());
        long tStart = System.currentTimeMillis();
        JSONArray result = resty.json(uri, payload).array();
        long tEnd = System.currentTimeMillis();
        long tReporting = tEnd - tStart;
        LOGGER.info("time (msec): " + tEnd/1000 + "," + tReporting);
        LOGGER.debug("result: " + result.toString());
    }
    

    private void addToBuffer(Measurement m) {
        if (array.length() <= bufferSize)
	    array.put(processMeasurement(m));
            
        else {
            // Send the grouped data and reinitialise the buffer and the counter
            LOGGER.debug("builder result: " + array.toString());

            try {
                if (queue.size() > 0.8*maxQueueLength)
                    LOGGER.warn("Queue size: " + queue.size());
                sendRequest();
            } catch (IOException | JSONException e) {
                LOGGER.error("Error while sending Measurement: " + e.getMessage());
              }
            finally {
                array = new JSONArray();
                array.put(processMeasurement(m));
                }
            }
    }
    
    
    
    private void dequeue() {
        LOGGER.info("Started " + Thread.currentThread().getName());
        Measurement m;
        while (!Thread.interrupted()) {
            try {
                m = queue.take();
                addToBuffer(m);
            } catch (InterruptedException ie) {
                LOGGER.info("Interrupted while waiting for a measurement");
                LOGGER.info("Terminated " + Thread.currentThread().getName());
            }
        }
        LOGGER.info("Terminated " + Thread.currentThread().getName());
    }
}
