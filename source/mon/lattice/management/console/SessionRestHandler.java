/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.console;

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
public class SessionRestHandler extends BasicRequestHandler {

    AbstractJSONRestController controllerInstance;
    private Logger LOGGER = LoggerFactory.getLogger(SessionRestHandler.class);
    
    public SessionRestHandler() {
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
                        addSession(request, response);
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name != null && segments.length == 2) {
                        deleteSession(request, response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "GET":
                    if (name == null && segments.length == 1)
                        getSessions(request, response);
                    else
                        if (segments.length == 2 && name != null)
                            getSessionInfo(request, response);
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
    
    
    
    private void addSession(Request request, Response response) throws JSONException, IOException {
        Query query = request.getQuery();
        
        String hostID;
        String userID;
        
        if (query.containsKey("host")) {
            hostID = query.get("host");
        } else {
            badRequest(response, "missing arg host");
            response.close();
            return;
        }
        
        if (query.containsKey("user")) {
            userID = query.get("user");
        } else {
            badRequest(response, "missing arg user");
            response.close();
            return;
        }

        boolean success = true;
        String failMessage = null;
        
        JSONObject jsobj = controllerInstance.createSession(hostID, userID);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("addSession: failure detected: " + failMessage);
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
    
    
    private void deleteSession(Request request, Response response) throws JSONException, IOException {
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        String sessionID;
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            sessionID = segments[1];
            jsobj = controllerInstance.deleteSession(sessionID);
        } else {
            badRequest(response, "not a valid ID format");
            response.close();
            return;
        }
        
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("deleteSession: failure detected: " + failMessage);
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
    
    
    private void getSessionInfo(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getSessionInfo");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getSessionInfo: failure detected: " + failMessage);
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
    
    
    private void getSessions(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getSessions");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getSessions: failure detected: " + failMessage);
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
