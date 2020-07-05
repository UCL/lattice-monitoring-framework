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
public class UserRestHandler extends BasicRequestHandler {

    AbstractJSONRestController controllerInstance;
    private Logger LOGGER = LoggerFactory.getLogger(UserRestHandler.class);
    
    public UserRestHandler() {
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
                        addUser(request, response);
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name != null && segments.length == 2) {
                        deleteUser(request, response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "GET":
                    if (name == null && segments.length == 1)
                        getUsers(request, response);
                    else
                        if (segments.length == 2 && name != null)
                            getUserInfo(request, response);
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
    
    
    
    private void addUser(Request request, Response response) throws JSONException, IOException {
        Query query = request.getQuery();
        
        String username;
        String authType;
        String token;
        
        if (query.containsKey("username")) {
            username = query.get("username");
        } else {
            badRequest(response, "missing arg username");
            response.close();
            return;
        }
        
        if (query.containsKey("type")) {
            authType = query.get("type");
        } else {
            badRequest(response, "missing arg type");
            response.close();
            return;
        }
        
        if (query.containsKey("token")) {
            token = query.get("token");
        } else {
            badRequest(response, "missing arg token");
            response.close();
            return;
        }

        boolean success = true;
        String failMessage = null;
        
        JSONObject jsobj = controllerInstance.addUser(username, authType, token);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("addUser: failure detected: " + failMessage);
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
    
    
    private void deleteUser(Request request, Response response) throws JSONException, IOException {
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        String userid;
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            userid = segments[1];
            jsobj = controllerInstance.deleteUser(userid);
        } else {
            badRequest(response, "not a valid id format");
            response.close();
            return;
        }
        
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("deleteUser: failure detected: " + failMessage);
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
    
    
    private void getUserInfo(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getUserInfo");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getUserInfo: failure detected: " + failMessage);
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
    
    
    private void getUsers(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = new JSONObject().put("Info", "Invoked getUsers");
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getUsers: failure detected: " + failMessage);
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
