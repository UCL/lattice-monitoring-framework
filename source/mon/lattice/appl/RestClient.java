/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import mon.lattice.control.ControlInterface;
import mon.lattice.management.ManagementInterface;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.delete;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.put;



public class RestClient implements ControlInterface<JSONObject>, ManagementInterface<JSONObject> { // ControlAgentsInterface
    protected String vimURI;
    protected Resty rest;
    protected int port;
    
    
    
    public RestClient(String addr, int port) throws IOException {
        this(InetAddress.getByName(addr), port);
    }
    
    public RestClient(InetAddress addr, int port) throws IOException {
        try {
            this.port = port;
            vimURI = "http://" + addr.getHostName() + ":" + Integer.toString(port);
            rest = new Resty();
        } catch (Exception e) {
            System.out.println("Error while initializing the rest client:" + e.getMessage());
        }
        
    }
    
    // POST http://localhost:6666/user/?username=uceeftu&type=KEY&token=%2FUsers%2Fuceeftu%2F.ssh%2Fid_rsa
    @Override
    public JSONObject addUser(String username, String type, String token) throws JSONException {
        try {
            String uri = vimURI + "/user/?username=" + username + "&type=" + type  + "&token=" + token;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("addUser FAILED" + " IOException: " + ioe.getMessage());
        }
            
    }

    
    // DELETE http://localhost:6666/user/3dd26d5d-73f5-4b02-aaa9-d35d029aa2c0
    @Override
    public JSONObject deleteUser(String userID) throws JSONException {
        try {
            String uri = vimURI + "/user/" + userID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deleteUser FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // POST http://localhost:6666/host/?hostname=localhost&port=22
    @Override
    public JSONObject addHost(String address, String port) throws JSONException {
        try {
            String uri = vimURI + "/host/?hostname=" + address + "&port=" + port;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("addHost FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // DELETE http://localhost:6666/host/bf8ba21e-bb2f-4e89-8bc6-af1f78cf7a36
    @Override
    public JSONObject removeHost(String hostID) throws JSONException {
        try {
            String uri = vimURI + "/host/" + hostID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("removeHost FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // POST http://localhost:6666/session/?host=bf8ba21e-bb2f-4e89-8bc6-af1f78cf7a36&user=0c2322c3-c468-434c-9ec9-1f9201f751ef
    @Override
    public JSONObject createSession(String hostID, String userID) throws JSONException {
        try {
            String uri = vimURI + "/session/?host=" + hostID + "&user=" +userID;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("createSession FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // DELETE http://localhost:6666/session/e427397c-0a42-4593-bb78-2c9648889f70
    @Override
    public JSONObject deleteSession(String sessionID) throws JSONException {
        try {
            String uri = vimURI + "/session/" + sessionID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deleteSession FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    

    //curl -X POST http://localhost:6666/datasource/?class=<className>\&session=<sessionID>\&args=arg1+arg2+argN
    @Override
    public JSONObject startDataSource(String className, String sessionID, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/?class=" + className + "&session=" + sessionID  + "&args=" + args;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deployDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X DELETE http://localhost:6666/datasource/<id>?session=<sessionID>
    @Override
    public JSONObject stopDataSource(String dataSourceID, String sessionID) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + dataSourceID + "?session=" + sessionID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // GET http://localhost:6666/datasource/<dsID>
    @Override
    public JSONObject getDataSourceInfo(String dsID) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + dsID;
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getDataSourceInfo FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X POST http://localhost:6666/datasource/<dsUUID>/probe/?className=<probeClassName>\&args=<arg1>+<arg2>+<argN>
    @Override
    public JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + id + "/probe/?className=" + probeClassName + "&args=" + URLEncoder.encode(probeArgs, "UTF-8");
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("loadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X DELETE http://localhost:6666/probe/<probeUUID>
    @Override
    public JSONObject unloadProbe(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("unloadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=off
    @Override
    public JSONObject turnOffProbe(String id) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + id + "/?status=off";
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnOffProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=on
    @Override
    public JSONObject turnOnProbe(String id) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + id + "/?status=on";
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnProbeOn FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?serviceid=<serviceUUID>
    @Override
    public JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?serviceid=" + serviceID;
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeServiceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?sliceid=<sliceUUID>
    @Override
    public JSONObject setProbeGroupID(String probeID, String groupID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?sliceid=" + groupID;
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeSliceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject setProbeDataRate(String probeID, String dataRate) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?datarate=" + dataRate;
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeDataRate FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject startDataConsumer(String className, String sessionID, String args) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/?class=" + className + "&session=" + sessionID  + "&args=" + args;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deployDC FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject stopDataConsumer(String dataSourceID, String sessionID) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/" + dataSourceID + "?session=" + sessionID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopDC FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject getDataConsumerMeasurementRate(String dcID) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/" + dcID + "/rate/";
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getDataConsumerMeasurementRate FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/" + id + "/reporter/?className=" + reporterClassName + "&args=" + URLEncoder.encode(reporterArgs, "UTF-8");
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("loadReporter FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject unloadReporter(String id) throws JSONException {
        try {
            String uri = vimURI + "/reporter/" + id;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("unloadReporter FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // GET http://localhost:6666/datasource/
    @Override
    public JSONObject getDataSources() throws JSONException {
        try {
            String uri = vimURI + "/datasource/";
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getDataSources FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // GET http://localhost:6666/dataconsumer/
    @Override
    public JSONObject getDataConsumers() throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/";
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getDataConsumers FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject getProbeDataRate(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/rate/";
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getProbeDataRate FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    // GET http://localhost:6666/probe/19fd5758-3bb5-4dc3-a72a-7950de17bae0/service/
    @Override
    public JSONObject getProbeServiceID(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/service/";
            JSONObject jsobj = rest.json(uri).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getProbeServiceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }


    @Override
    public JSONObject startControllerAgent(String className, String args, String sessionID) throws Exception {
        try {
            String uri = vimURI + "/controlleragent/?class=" + className + "&session=" + sessionID  + "&args=" + args;
            JSONObject jsobj = rest.json(uri, form("")).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("startControllerAgent FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    

    @Override
    public JSONObject stopControllerAgent(String caID, String sessionID) throws Exception {
        try {
            String uri = vimURI + "/controlleragent/" + caID + "?session=" + sessionID;
            JSONObject jsobj = rest.json(uri, delete()).toObject();
            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopControllerAgent FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    
    @Override
    public void init() {
        throw new UnsupportedOperationException("This method is only supported by the Controller");
    }
    
    
    // should also implement methods from controller Agent interface
    
}
