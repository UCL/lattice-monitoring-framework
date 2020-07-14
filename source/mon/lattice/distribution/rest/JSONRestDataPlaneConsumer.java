package mon.lattice.distribution.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import mon.lattice.core.Measurement;
import mon.lattice.core.TypeException;
import mon.lattice.distribution.DataPlaneMessageJSONDecoder;
import mon.lattice.distribution.MetaData;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * A JSONRestDataPlaneConsumer is a DataPlane implementation
 * that receives Measurements via REST.
 * The message encoding is based on JSON.
 */
public class JSONRestDataPlaneConsumer extends AbstractRestDataPlaneConsumer {
    
    private static Logger LOGGER = LoggerFactory.getLogger(JSONRestDataPlaneConsumer.class);;

    public JSONRestDataPlaneConsumer(int port, String endP) throws IOException {
        super(port, endP);
    }

    public JSONRestDataPlaneConsumer(String remoteHost, int port, String endP) throws IOException {
        super(remoteHost, port, endP);
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
            String reqEndPoint;
            
            long time = System.currentTimeMillis();

            response.set("Content-Type", "application/json");
            response.set("Server", "JSONRestDataPlaneConsumer");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            
            if (method.equals("POST")) {
                if (name == null && segments.length == 1) {
                    reqEndPoint = segments[0];
                    if (reqEndPoint.equals(endPoint)) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(request.getContent().getBytes());
                        RestTransmissionMetaData metaData = new RestTransmissionMetaData(request.getContentLength(),
                                                                                         request.getClientAddress().getAddress(),
                                                                                         address.getAddress(), 
                                                                                         request.getClientAddress().getPort()
                                                                                        );
                        received(bis, metaData);
                        reply.put("Accepted", true);
                    }
                    else {
                        reply.put("Accepted", false);
                        LOGGER.error("EndPoint " + reqEndPoint + " is not valid");  
                    }
                }
                else {
                    reply.put("Accepted", false);
                    LOGGER.error("Malformed URI");    
                }
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
            DataPlaneMessageJSONDecoder decoder = new DataPlaneMessageJSONDecoder(getSeqNoMap());
            Measurement measurement = decoder.decode(bis, metaData);

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
