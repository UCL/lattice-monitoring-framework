package mon.lattice.control.im;

import java.io.IOException;
import java.net.InetAddress;
import mon.lattice.core.ID;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractControlEndPointMetaData implements ControlEndPointMetadata {
    protected final String type;

    public AbstractControlEndPointMetaData(String type) {
        this.type = type;
    }
    
    @Override
    public String getType() {
        return type;
    }  
    
    
    public static AbstractControlEndPointMetaData newInstance(Object rawControlEndPointInfo, ID entityID) throws IOException {
        JSONObject controlEndPointInfo;
        
        try {
            // this is usually stored into the DHT info plane as a string
            if (rawControlEndPointInfo instanceof String) { 
                controlEndPointInfo = new JSONObject();
                
                //example -> type:zmq;address:localhost;port:2233
                String[] controlEndPointFields = ((String) rawControlEndPointInfo).split(";");
                
                String[] type;
                String[] address;
                String[] port;
                
                switch (controlEndPointFields.length) {
                    case 1:
                        type = controlEndPointFields[0].split(":");
                        controlEndPointInfo.put(type[0], type[1]);
                        break;
                    case 3:
                        type = controlEndPointFields[0].split(":");
                        controlEndPointInfo.put(type[0], type[1]);
                        
                        address = controlEndPointFields[1].split(":");
                        controlEndPointInfo.put(address[0], address[1]);
                            
                        port = controlEndPointFields[2].split(":");
                        controlEndPointInfo.put(port[0], port[1]);
                        break;
                    default:
                        //throw error
                        break;
                }
                
            }
            
            // this is stored in the ZMQ info plane as a JSONObject
            else
                controlEndPointInfo = (JSONObject)rawControlEndPointInfo;
            
            AbstractControlEndPointMetaData controlEndPointMetaData = null;
            if (controlEndPointInfo.getString("type").equals("socket")) {
                controlEndPointMetaData = new SocketControlEndPointMetaData(controlEndPointInfo.getString("type"),
                                                        InetAddress.getByName(controlEndPointInfo.getString("address")),
                                                        Integer.valueOf(controlEndPointInfo.getString("port"))
                                                       );
            }
            
            else if (controlEndPointInfo.getString("type").equals("zmq")) {
                controlEndPointMetaData = new ZMQControlEndPointMetaData(controlEndPointInfo.getString("type"), entityID);
            }
            
            return controlEndPointMetaData;
        }
        
        catch(Exception e) {
            throw new IOException("error while parsing controlEndPoint information: " + e.getMessage());
        }
        
    }
}
