/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.controller.json;

import mon.lattice.control.console.JSONControllerManagementConsole;
import mon.lattice.control.console.ManagementConsoleWithPoolSize;
import mon.lattice.control.console.RestConsoleInterface;
import mon.lattice.management.deployment.ssh.AuthType;
import mon.lattice.management.deployment.DeploymentException;
import mon.lattice.management.deployment.ssh.SSHDeploymentManager;
import mon.lattice.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONArray;
import mon.lattice.management.deployment.DeploymentInterface;

/**
 * Extends the AbstractController and implements
 * the deployment functions and a JSON based REST API
 * @author uceeftu
 */
public abstract class AbstractJSONRestController extends AbstractJSONController implements DeploymentInterface<JSONObject>, RestConsoleInterface {
    protected SSHDeploymentManager deploymentManager;
    protected Boolean usingDeploymentManager = true;
    
    protected String localJarPath;
    protected String jarFileName;
    protected String remoteJarPath;
    
    protected int restConsolePort;
    protected ManagementConsoleWithPoolSize JSONManagementConsole = null;
    
    
    
    private static Logger LOGGER;
    
    
    protected AbstractJSONRestController() {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        LOGGER = LoggerFactory.getLogger(AbstractJSONRestController.class);
    }
    
    
    @Override
    abstract public void initPlanes();
    
    
    @Override
    public void initRESTConsole() {
        restConsolePort = Integer.parseInt(pr.getProperty("restconsole.localport"));
        poolSize = Integer.parseInt(pr.getProperty("control.poolsize"));
        JSONManagementConsole=new JSONControllerManagementConsole(this, restConsolePort);
        JSONManagementConsole.start(poolSize);  
    }
    
    
    @Override
    public void init() {
        
        localJarPath = pr.getProperty("deployment.localJarPath");
        jarFileName = pr.getProperty("deployment.jarFileName");
        remoteJarPath = pr.getProperty("deployment.remoteJarPath");
        
        if (localJarPath != null && jarFileName != null && remoteJarPath != null) {
            if (this.usingDeploymentManager) {
                deploymentManager = new SSHDeploymentManager(localJarPath, jarFileName, remoteJarPath);
                deploymentManager.setControlInformation(controlInformationManager);
                LOGGER.info("Deployment Manager has been activated");
            }
            else {
                LOGGER.warn("Deployment Manager has not been activated");
                this.usingDeploymentManager = false;
            }
        }
        
    }

    @Override
    public JSONObject addUser(String username, String type, String token) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID addedUserID;
        
        result.put("operation", "addUser");

        if (this.usingDeploymentManager) {
            try { 
                addedUserID = this.deploymentManager.addUser(username, AuthType.valueOf(type), token);
                result.put("ID", addedUserID.toString());
                result.put("success", true);
            } catch (DeploymentException ex) {  
                result.put("success", false);
                result.put("msg", "UserException while performing addUser operation: " + ex.getMessage());
            } 
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        
        return result;        
    }

    @Override
    public JSONObject deleteUser(String userID) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "deleteUser");
        result.put("ID", userID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.deleteUser(ID.fromString(userID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "Error while performing deleteUser operation: " + ex.getMessage());
            }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    
    @Override
    public JSONObject addHost(String address, String port) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID addedHostID;
        
        result.put("operation", "addHost");

        if (this.usingDeploymentManager) {
            try { 
                addedHostID = this.deploymentManager.addHost(address, Integer.valueOf(port));
                result.put("ID", addedHostID.toString());
                result.put("success", true);
            } catch (DeploymentException ex) { 
                result.put("success", false);
                result.put("msg", "Error while performing addHost operation: " + ex.getMessage());
            } 
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;        
    }

    @Override
    public JSONObject removeHost(String hostID) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "removeHost");
        result.put("ID", hostID);
        
        if (this.usingDeploymentManager) {
            try { 
                this.deploymentManager.removeHost(ID.fromString(hostID));
                result.put("success", true);                
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "Error while performing removeHost operation: " + ex.getMessage());
            }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }

    
    
    @Override
    public JSONObject createSession(String hostID, String userID) throws JSONException {
       JSONObject result = new JSONObject();
        
        ID createdSessionID;
        
        result.put("operation", "createSession");
        result.put("hostID", hostID);
        result.put("userID", userID);

        if (this.usingDeploymentManager) {
            try {
                createdSessionID = this.deploymentManager.createSession(ID.fromString(hostID), ID.fromString(userID)); // TODO classname might be passed as arg

                if (createdSessionID == null) {
                    result.put("msg", "en error occured while creating session");
                    result.put("success", false);
                }

                else {
                    result.put("ID", createdSessionID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "Error while performing createSession operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }

    @Override
    public JSONObject deleteSession(String sessionID) throws JSONException {
         JSONObject result = new JSONObject();
        
        result.put("operation", "deleteSession");
        result.put("ID", sessionID);
        
        if (this.usingDeploymentManager) {
            try {
                deploymentManager.deleteSession(ID.fromString(sessionID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "Error while performing deleteSession operation: " + ex.getMessage());
            }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;

    }
    
    
    
    

    @Override
    public JSONObject startDataSource(String className, String args, String sessionID) throws JSONException {
        
        JSONObject result = new JSONObject();
        
        ID startedDsID;
        
        result.put("operation", "startDataSource");
        result.put("endpoint", sessionID);

        if (this.usingDeploymentManager) {
            try {
                startedDsID = this.deploymentManager.startDataSource(className, args, ID.fromString(sessionID)); // TODO classname might be passed as arg

                if (startedDsID == null) {
                    result.put("msg", "en error occured while starting the Data Source on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDsID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "Error while performing startDataSource operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }

      
    @Override
    public JSONObject stopDataSource(String dataSourceID, String sessionID) throws JSONException {
            
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDataSource");
        result.put("ID", dataSourceID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataSource(ID.fromString(dataSourceID), ID.fromString(sessionID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "Error while performing stopDataSource operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    @Override
    public JSONObject startDataConsumer(String className, String args, String sessionID) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDcID;
        
        result.put("operation", "startDataConsumer");
        result.put("endpoint", sessionID);

        if (this.usingDeploymentManager) {
            try {
                startedDcID = this.deploymentManager.startDataConsumer(className, args, ID.fromString(sessionID));

                if (startedDcID == null) {
                    result.put("msg", "en error occured while starting the Data Consumer on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDcID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "Error while performing startDataConsumer operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    } 
    

    @Override
    public JSONObject stopDataConsumer(String dataConsumerID, String sessionID) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDataConsumer");
        result.put("ID", dataConsumerID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataConsumer(ID.fromString(dataConsumerID), ID.fromString(sessionID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "Error while performing stopDataConsumer operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    @Override
    public JSONObject getDataSources() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataSources");
        
        try {
            JSONArray dataSources = this.deploymentManager.getDataSources();
            result.put("datasources", dataSources);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataSources operation: " + ex.getMessage());
          }
        return result;  
    }
    
    
    @Override
    public JSONObject getDataConsumers() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataConsumers");
        
        try {
            JSONArray dataConsumers = this.deploymentManager.getDataConsumers();
            result.put("dataconsumers", dataConsumers);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataConsumers operation: " + ex.getMessage());
          }
        return result;  
    }
    

    @Override
    public JSONObject startControllerAgent(String className, String args, String sessionID) throws Exception {
        JSONObject result = new JSONObject();
        result.put("operation", "startControllerAgent");
        result.put("success", false);
        result.put("msg", "Not supported by this controller " + this.getClass().getName());
        return result;
    }

    @Override
    public JSONObject stopControllerAgent(String id, String sessionID) throws Exception {
        JSONObject result = new JSONObject();
        result.put("operation", "stopControllerAgent");
        result.put("success", false);
        result.put("msg", "Not supported by this controller " + this.getClass().getName());
        return result;
    }

    @Override
    public JSONObject getControllerAgents() throws Exception {
        JSONObject result = new JSONObject();
        result.put("operation", "getControllerAgents");
        result.put("success", false);
        result.put("msg", "Not supported by this controller " + this.getClass().getName());
        return result;
    }
    
    

}
