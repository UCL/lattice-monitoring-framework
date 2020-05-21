package eu.fivegex.monitoring.test;

import mon.lattice.appl.RestClient;
import mon.lattice.core.ID;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbesCatalogue;

/**
 * A Test of the basic Lattice components for the 5GEx CI/CD validation
 **/
public class LatticeTest extends RestClient implements JSONProbesCatalogue {
    
    String username;
    String userKey;
    String userID;
    
    //general attributes
    String DSEndPointAddress;
    String DSEndPointName;
    String DSEndPointPort;
    String DSHostID;
    String DSHostSessionID;
    String DSClassName;
    
    String DCEndPointAddress;
    String DCEndPointName;
    String DCEndPointPort;
    String DCHostID;
    String DCHostSessionID;
    String DCClassName;

    String DCDataPlaneAddress;
    String DCDataPlanePort;
    
    String controllerInfoPlaneAddress;
    String controllerInfoPlanePort;
    
    String DSInfoPlanePort;
    String controllerControlPlanePort;
    
    String DCInfoPlanePort;
    String DCControlPlanePort;
    
    //docker related attributes
    String dockerHost;
    String dockerPort;
    String dockerContainerID;
    String dockerContainerName;
    
    String mongoAddress;
    String mongoPort;
    String mongoDBName;
    String mongoCollection;
    

      
    /**
     * Construct a LatticeTest
     * using defaults of localhost and port 6666
     */
    public LatticeTest(Properties configuration) throws IOException {
        this(configuration.getProperty("controller.infoplane.address"), Integer.valueOf(configuration.getProperty("controller.rest.port")));
        
        username = configuration.getProperty("user.id");
        userKey = configuration.getProperty("user.key");
        
        controllerInfoPlaneAddress = configuration.getProperty("controller.infoplane.address");
        controllerInfoPlanePort = configuration.getProperty("controller.infoplane.port");
        controllerControlPlanePort = configuration.getProperty("controller.controlplane.port");
               
        DSEndPointAddress = configuration.getProperty("ds.endpoint.address");
        DSEndPointName = configuration.getProperty("ds.endpoint.name");
        DSClassName = configuration.getProperty("ds.class");
        
        DCEndPointAddress = configuration.getProperty("dc.endpoint.address");
        DCEndPointName = configuration.getProperty("dc.endpoint.name");
        DCClassName = configuration.getProperty("dc.class");

        DCDataPlaneAddress = configuration.getProperty("dc.dataplane.address");
        DCDataPlanePort = configuration.getProperty("dc.dataplane.port");
        
        try {
            dockerHost = configuration.getProperty("dockerHost");
            dockerPort = configuration.getProperty("dockerPort");
            dockerContainerID = configuration.getProperty("dockerContainerID");
            dockerContainerName = configuration.getProperty("dockerContainerName");   

            mongoAddress = configuration.getProperty("mongodb.address");
        } catch (Exception e) {
            System.out.println("Warning: Docker / MongoDB configuration error");
        }
        
        mongoPort = "27017";
        mongoDBName = "test";
        mongoCollection = "cs";
        
        DSEndPointPort = "22";
        DCEndPointPort= "22";
    }

 
    
    public LatticeTest(String addr, int port) throws IOException  {
        super(addr, port);
    }
    
    
    
    // curl -X GET http://localhost:6666/probe/catalogue/
    @Override
    public JSONObject getProbesCatalogue() throws JSONException {
        try {
            String uri = vimURI + "/probe/catalogue/";
            
            JSONObject jsobj = rest.json(uri).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getProbesCatalogue FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    
    
    private void testMemoryInfoProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception {
        String probeClassName = "eu.fivegex.monitoring.appl.probes.MemoryInfoProbe";
        JSONObject out = new JSONObject();
        
        try {
            System.out.println("Creating probe on endpoint: " + DSEndPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbe(dsID, probeClassName, probeName);
            
            String probeID = out.getString("createdProbeID");

            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = setProbeServiceID(probeID, serviceID);

            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = setProbeGroupID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = turnOnProbe(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + DSEndPointName + " - DS id: " + dsID);
            out = turnOffProbe(probeID);
        }
        catch (InterruptedException ex) {
            return;
        } 
        catch (JSONException ex) {
            throw new Exception("Test Case MemoryInfoProbe Failed! " + "\nReason: " + out.getString("msg"));
        }  
    }
    
    private void testDockerProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception { 
        String probeClassName = "eu.fivegex.monitoring.appl.probes.docker.DockerProbe";
        JSONObject out;
        
        try {
            System.out.println("Creating probe on endpoint: " + DSEndPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbe(dsID, probeClassName, dockerHost + "+" +
                                     dockerPort + "+" +
                                     probeName + "+" +
                                     dockerContainerID + "+" +
                                     dockerContainerName
                                    );
            
            String probeID = out.getString("createdProbeID");
            
            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            setProbeServiceID(probeID, serviceID);
            
            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            setProbeGroupID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            turnOnProbe(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + DSEndPointName + " - DS id: " + dsID);
            turnOffProbe(probeID);   
        }
        catch (InterruptedException ex) {
            return;
        }
        catch (JSONException ex) {
            throw new Exception("Test Case DockerProbe Failed! " + "\nReason: " + ex.getMessage());
        }
    }
    
    
    private void addUser() throws Exception {
        JSONObject out = null;
        try {
            out = addUser(username, "KEY", userKey);
            userID = out.getString("ID");
        } catch (JSONException e) {
            throw new Exception("Error while instantiating DS\n" + out.getString("msg"));
        }
    }
    
    
    protected String instantiateDS() throws Exception {
        JSONObject out = new JSONObject();
        
        System.out.println("Deploying DS on endpoint: " + DSEndPointName);
        
        try {
            out = addHost(DSEndPointAddress, DSEndPointPort);
            DSHostID = out.getString("ID");
            
            out = createSession(DSHostID, userID);
            DSHostSessionID = out.getString("ID");
            
            out = startDataSource(DSClassName, DSHostSessionID, DCDataPlaneAddress + "+" + 
                                                                DCDataPlanePort + "+" +
                                                                controllerInfoPlaneAddress + "+" +
                                                                controllerInfoPlanePort + "+" +
                                                                controllerControlPlanePort
                                );
            
            return out.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DS\n" + out.getString("msg"));
        }
    }
    
    
    protected void unloadDS(String dsID) throws Exception {
        JSONObject out;
        System.out.println("Stopping DS on endpoint: "  + DSEndPointAddress + " - DS id: " + dsID);
        try {
            out = stopDataSource(dsID, DSHostSessionID);  
    
            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping DS: " + dsID + out.getString("msg")); 
            
            out = deleteSession(DSHostSessionID);
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading DS: " + e.getMessage());
        }
    }
    
    
    protected String instantiateDC() throws Exception { // we should create a Lattice test exception 
        JSONObject out = new JSONObject();
        
        System.out.println("Deploying DC on endpoint: " + DCEndPointName);
        
        try {
            out = addHost(DCEndPointAddress, DCEndPointPort);
            DCHostID = out.getString("ID");
            
            out = createSession(DCHostID, userID);
            DCHostSessionID = out.getString("ID");
            
            out = startDataConsumer(DCClassName, DCHostSessionID,   DCDataPlanePort + "+" +
                                                                    controllerInfoPlaneAddress + "+" +
                                                                    controllerInfoPlanePort + "+" +
                                                                    controllerControlPlanePort
                                    );
            
            return out.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DC\n" + out.getString("msg"));
        }
    }
    
    
    protected void unloadDC(String dcID) throws Exception {
        JSONObject out;
        System.out.println("Stopping DC on endpoint: "  + DCEndPointAddress + " - DC id: " + dcID);
        try {
            out = stopDataConsumer(dcID, DCHostSessionID);
    
            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping DC: " + dcID + out.getString("msg")); 
            
            out = deleteSession(DSHostSessionID);
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading DC: " + e.getMessage());
        }
    }
    
    
    private String loadMongoDBReporter(String dcID) throws Exception {
        String reporterClassName = "eu.fivegex.monitoring.appl.reporters.MongoDBReporter";
        
        JSONObject out;
        
        try {
            System.out.println("Starting reporter on endpoint: " + DCEndPointName + " - DC id: " + dcID);
            System.out.println("Dinamically loading reporter class: " + reporterClassName);
            
            out = loadReporter(dcID, reporterClassName, mongoAddress + "+" +
                                     mongoPort + "+" +
                                     mongoDBName + "+" +
                                     mongoCollection
                                    );
            
            String reporterID = out.getString("createdReporterID");  
            return reporterID;
        }

        catch (JSONException ex) {
            throw new Exception("Test Case loadMongoDBReporter Failed! " + "\nReason: " + ex.getMessage());
        }
        
        
    }
    
    
    private MongoDBInteracter createMongoDBEntry(String serviceID, String probeName) throws JSONException, ParseException, IOException {
        
        JSONObject obj = new JSONObject();
        obj.put("agreementId", serviceID);
        obj.put("name", "Lattice Test");
        obj.put("maxResult", 10);
        obj.put("kpiList", new JSONArray().put(probeName));
        
        MongoDBInteracter mongo = new MongoDBInteracter(mongoAddress, Integer.valueOf(mongoPort), mongoCollection);
        mongo.createMongoDBEntry(obj);
        return mongo; //just a bad thing
    }
  
    
    private void removeHosts() throws Exception {
        try {
            super.removeHost(DSHostID);
        } catch (JSONException je) {
            throw new Exception("Error removing host: " + DSHostID + " " + je.getMessage());
        }
        
        try {
            super.removeHost(DCHostID);
        } catch (JSONException je) {
            throw new Exception("Error removing host: " + DCHostID + " " + je.getMessage());
        }
    }
    
    
    
    @Override
    public void initCatalogue() {
        throw new UnsupportedOperationException("This method is only supported by the Controller");
    }
    
    
    
    
    public static void main(String[] args) {
        LatticeTest test = null;
        String dsID = null;
        String dcID = null;
        String reporterID = null;
        
        String sessionID = null;
        
        boolean errorStatus = false;
        
        try {
            Properties configuration = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            if (args.length == 0)
                propertiesFile = System.getProperty("user.home") + "/latticeTest.properties";
            else if (args.length == 1)
                propertiesFile = args[0];
            else {
                System.out.println("Please use: java LatticeTest [file.properties]");
                System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            configuration.load(input);
            
            test = new LatticeTest(configuration);

            test.addUser();
            // instantiating a new DS on the endpoint as per configuration (field DSEndPointAddress)
            dsID = test.instantiateDS();
            
            dcID = test.instantiateDC();
            
            
            //reporterID = test.loadMongoDBReporter(dcID);
            
            // generating service/slice IDs to be associated to all the test probes
            String serviceID = ID.generate().toString();
            String sliceID = ID.generate().toString();
            String probeName = "testMemoryProbe";
            
            // creating entry in DB
            //MongoDBInteracter m = test.createMongoDBEntry(serviceID, probeName);
            
            // instantiating some test probes on the previous DS
            test.testMemoryInfoProbe(probeName, dsID, serviceID, sliceID);
            //client.testDockerProbe("testDockerProbe", dsID, serviceID, sliceID);

            //Document mongoDBEntry = m.getMongoDBEntry(serviceID, probeName); // TODO: incorrect this should also check id the ProbeName element is not empty
            System.out.println("Reading data (1 measurement) from the DB related to the previous service " + serviceID + " and " + probeName);
            //if (mongoDBEntry != null)
            //    System.out.println(mongoDBEntry.toJson(new JsonWriterSettings(true)));
            //else
            //    throw new Exception("Cannot find any entries with service ID " + serviceID + " in the DB");
           
        }
        catch (Exception e) {
            System.out.println("\n************************************************** TEST FAILED **************************************************\n" + 
                               e.getMessage() + 
                               "\n*****************************************************************************************************************\n");
            errorStatus = true;
        }
        finally {
            // trying to stop the previous instantiated DS/DC anyway
            try {
                if (test != null) {
                    if (dsID != null)
                        test.unloadDS(dsID);
                    if (dcID != null)  {
                        if (reporterID != null) {
                            System.out.println("Unloading Reporter " + reporterID);
                            test.unloadReporter(reporterID);
                        }
                        test.unloadDC(dcID);
                    }
                }
            }
            catch (Exception e) { // the DS/DC was either already stopped or not running
            }
        }
    if (errorStatus)
        System.exit(1);
    }

    
    
}
