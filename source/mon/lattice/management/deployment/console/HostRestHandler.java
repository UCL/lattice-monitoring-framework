/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment.console;

import cc.clayman.console.BasicRequestHandler;
import java.io.IOException;
import java.io.PrintStream;
import mon.lattice.control.controller.json.AbstractJSONRestController;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class HostRestHandler extends BasicRequestHandler {

    AbstractJSONRestController controllerInstance;
    private Logger LOGGER = LoggerFactory.getLogger(HostRestHandler.class);
    
    public HostRestHandler() {
    }
    
    
     @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        controllerInstance = (AbstractJSONRestController) getManagementConsole().getAssociated();
        
        LOGGER.debug("-------- REQUEST RECEIVED --------\n" + request.getMethod() + " " +  request.getTarget());
        
        
        long time = System.currentTimeMillis();
        
        response.set("Content-Type", "application/json");
        response.set("Server", "LatticeController/1.0 (SimpleFramework 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        // get the path
        Path path = request.getPath();
        path.getDirectory();
        String name = path.getName();
        String[] segments = path.getSegments();

        // Get the method
        String method = request.getMethod();
        
        try {
            switch (method) {
                case "POST":
                    if (name == null && segments.length == 1)
                       addHost(request, response);
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name != null && segments.length == 2) {
                        removeHost(request,response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "GET":
                    if (name == null && segments.length == 1)
                        getHosts(request, response);
                    else
                        if (segments.length == 2 && name != null)
                            getHostInfo(request, response);
                        else
                            notFound(response, "GET bad request");
                    break;   
                default:
                    badRequest(response, "Unknown method " + method);
                    return false;
            }
            
            
            return true;
            
            } catch (IOException ex) {
                LOGGER.error("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                LOGGER.error("JSONException" + jex.getMessage());
            }
             finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                LOGGER.error("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }
    
    
    
    private void addHost(Request request, Response response) throws JSONException, IOException {
        Path path = request.getPath();
        Query query = request.getQuery();
        
        String hostname;
        String port;
        
        if (query.containsKey("hostname")) {
            hostname = query.get("hostname");
        } else {
            badRequest(response, "missing arg hostname");
            response.close();
            return;
        }
        
        if (query.containsKey("port")) {
            port = query.get("port");
        } else {
            badRequest(response, "missing arg port");
            response.close();
            return;
        }
       

        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInstance.addHost(hostname, port);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("addHost: failure detected: " + failMessage);
            success = false;   
        }   
    
        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
    }
    
    
    private void removeHost(Request request, Response response) throws JSONException, IOException {
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        String hostID;
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            hostID = segments[1];
            jsobj = controllerInstance.removeHost(hostID);
        } else {
            badRequest(response, "not a valid id format");
            response.close();
            return;
        }
        
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("removeHost: failure detected: " + failMessage);
            success = false;   
        }   
    
        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
    }
    
    
    private void getHostInfo(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getHostInfo");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getHostInfo: failure detected: " + failMessage);
            success = false;   
        }   
    
        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
    }
    
    
    private void getHosts(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getHosts");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getHosts: failure detected: " + failMessage);
            success = false;   
        }   
    
        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
    }
    
    
}