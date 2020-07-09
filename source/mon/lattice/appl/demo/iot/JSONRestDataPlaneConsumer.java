package mon.lattice.appl.demo.iot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.MeasurementDecoderJSON;
import mon.lattice.distribution.MetaData;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


public class JSONRestDataPlaneConsumer extends AbstractRestDataPlaneConsumer {
    
    private static Logger LOGGER = LoggerFactory.getLogger(JSONRestDataPlaneConsumer.class);;

    public JSONRestDataPlaneConsumer(int port) throws IOException {
        super(port);
    }

    public JSONRestDataPlaneConsumer(String remoteHost, int port) throws IOException {
        super(remoteHost, port);
    }
    
    
    @Override
    public void handle(Request request, Response response) {
        try {
            PrintStream body = response.getPrintStream();
            String method = request.getMethod();
            Path path = request.getPath();
            String name = path.getName();
            String[] segments = path.getSegments();
            JSONObject reply = new JSONObject();
            
            long time = System.currentTimeMillis();

            response.set("Content-Type", "application/json");
            response.set("Server", "JSONRestDataPlaneConsumer");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            
            if (method.equals("POST") && name == null && segments.length == 1) {
                ByteArrayInputStream bis = new ByteArrayInputStream(request.getContent().getBytes());
                received(bis, null); // metadata is not used
                reply.put("Accepted", true);
            } 
            
            else {
                reply.put("Accepted", false);
                LOGGER.error("POST is the only supported method");
            }
            body.println(reply);
            body.close();
        } catch(IOException | TypeException | JSONException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        try {
            int avail = bis.available();
            byte[] bytes = new byte[avail];
            bis.read(bytes, 0, avail);
            String str = new String(bytes);

            JSONObject json = new JSONObject(str);
            LOGGER.debug(json.toString());
            
            MeasurementDecoderJSON decoder = new MeasurementDecoderJSON();
            Measurement measurement = decoder.decode(json);

            report(measurement);

	} catch (JSONException ioe) {
	    LOGGER.error("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw new IOException(ioe.getMessage());
	} catch (Exception e) {
	    LOGGER.error("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
            throw new TypeException(e.getMessage());
	}
        
    }
    
    
    
    
}
