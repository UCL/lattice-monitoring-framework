package mon.lattice.appl.demo.iot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import mon.lattice.appl.RestClient;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class IotTopology {
    Properties configuration;
    RestClient restClient;
    List<String> entityIDs;
    Integer topologyId;
    
    String userID;
    String hostID;
    
    String controllerAddress;
    String controllerInfoPlanePort;
    String controllerControlPlanePort;  
    
    Integer dsNumber;
    Integer nSensors;
    Integer rate;
    Integer waitMin;
    Integer waitMax;
    Integer valueMin;
    Integer valueMax;

    Integer bufferSize;
    String reporterAddress;
    String reporterPort;
    String reporterURI;
    Integer topologies;

    String dataConsumerID;
    String dsClassName;
    String dcDataplaneAddress;
    Integer dcDataPlanePort;
    String dsHostSessionID;
    
    List<String> dataSourceIDs = new ArrayList<>();
    String dcClassName;
    String dcHostSessionID; 
    
    String reporterID;
    Map<String, List<String>> dataSourceProbesIDs = new HashMap<>();
    
    Thread currentThread;
    

    public IotTopology(int topologyId,
                       String userID,
                       String hostID,
                       int dsNumber,
                       int nSensors,
                       int rate,
                       int waitMin,
                       int waitMax,
                       int valueMin,
                       int valueMax,
                       int bufferSize,
                       String reporterAddress,
                       String reporterPort,
                       String reporterURI,
                       String controllerAddress,
                       int controllerPort) throws IOException
       {
        this.topologyId = topologyId;
        this.userID = userID;
        this.hostID = hostID;
        this.dsNumber = dsNumber;
        this.nSensors = nSensors;
        this.rate = rate;
        this.waitMin = waitMin;
        this.waitMax = waitMax;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.bufferSize = bufferSize;
        this.reporterAddress = reporterAddress;
        this.reporterPort = reporterPort;
        this.reporterURI = reporterURI;
        this.controllerAddress = controllerAddress;
        this.restClient = new RestClient(controllerAddress, controllerPort);
       }
    
    
    public void loadConfiguration() {
        controllerInfoPlanePort = configuration.getProperty("controller.infoplane.port");
        controllerControlPlanePort = configuration.getProperty("controller.controlplane.port");
        
        dcDataplaneAddress = configuration.getProperty("host.address");
        dsClassName = configuration.getProperty("ds.class");
        dcClassName = configuration.getProperty("dc.class");
        
        // in order to allocate a different Data Consumer reporterPort for each topology 
        // we add the topology ID to the value read from the conf file
        dcDataPlanePort = Integer.valueOf(configuration.getProperty("dc.dataplane.port")) + topologyId;
    }
    
    
    public void setProperties(Properties configuration) {
        this.configuration = configuration;
    }
    
    
    public void setEntitiesIDs(List l) {
        this.entityIDs = l;
    }
    
    
    private String getRandomEntityID() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, entityIDs.size());
        return entityIDs.get(randomIndex);
    }
    
    
    private void startDataSource() throws Exception {        
        System.out.println("Deploying Data Source on host: " + hostID);
        
        try {
            JSONObject startDS = restClient.startDataSource(dsClassName, dsHostSessionID, dcDataplaneAddress + "+" + 
                                                                                          dcDataPlanePort + "+" +
                                                                                          controllerAddress + "+" +
                                                                                          controllerInfoPlanePort + "+" +
                                                                                          controllerControlPlanePort
                                                            );
            
            dataSourceIDs.add(startDS.getString("ID"));
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DS\n" + e.getMessage());
        }
    }
    
    
    private void startDataConsumer() throws Exception { 
        System.out.println("Deploying Data Consumer on host: " + hostID);
        
        try {
            JSONObject startDC = restClient.startDataConsumer(dcClassName, dcHostSessionID, dcDataPlanePort + "+" +
                                                                                            controllerAddress + "+" +
                                                                                            controllerInfoPlanePort + "+" +
                                                                                            controllerControlPlanePort
                                                            );
            
            dataConsumerID = startDC.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DS\n" + e.getMessage());
        }
        
        
    }
    
    
    private void stopDataSource(String dsID) throws Exception {
        JSONObject out;
        System.out.println("Stopping Data Source on host: "  + hostID + " - DS id: " + dsID);
        try {
            out = restClient.stopDataSource(dsID, dsHostSessionID);  
    
            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping Data Source: " + dsID + out.getString("msg"));
            
            //dataSourceIDs.remove(dsID);
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading Data Source: " + e.getMessage());
        }
    }
   
    
    private void stopDataConsumer() throws Exception {
        JSONObject out;
        System.out.println("Stopping Data Consumer on host: "  + hostID + " - DS id: " + dataConsumerID);
        try {
            out = restClient.stopDataConsumer(dataConsumerID, dcHostSessionID);  
    
            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping Data Consumer: " + dataConsumerID + out.getString("msg"));
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading Data Consumer: " + e.getMessage());
        }
    }
    
    
    private void loadSensor(String dataSourceID, String probeName, List<String> probes)  {
        String probeClassName = "mon.lattice.appl.demo.SensorEmulatorProbe";
        String probeAttributeName = "temperature";
        
        Integer value = ThreadLocalRandom.current().nextInt(valueMin, valueMax);
        try {
            JSONObject out = restClient.loadProbe(dataSourceID, probeClassName, probeName + "+" + probeAttributeName + "+" + value + "+" + rate);
            String probeID = out.getString("createdProbeID");
            probes.add(probeID);
        } catch (JSONException je) {
            System.err.println("Topology " + topologyId + ": Error while loading Probe: " + probeName + " on DataSource " + dataSourceID);
        }
    }
    
    
    private void activateSensor(String dataSourceID, String probeID)  throws InterruptedException {
        // waiting a random time in msecs before activating the sensor / probe
        int randomWait = ThreadLocalRandom.current().nextInt(waitMin, waitMax);
        Thread.sleep(randomWait);
        try {
        restClient.setProbeServiceID(probeID, getRandomEntityID());
        restClient.turnOnProbe(probeID);
        } catch (JSONException je) {
            System.err.println("Topology " + topologyId + ": Error while activating Probe: " + probeID + " on DataSource " + dataSourceID);
        }
    }
    
    
    private String loadReporter(String reporterName) {
        String reporterClassName = "mon.lattice.appl.reporters.BufferedRestReporter";
        
        JSONObject out;
        
        try {

            out = restClient.loadReporter(dataConsumerID, reporterClassName, 
                                        reporterName + "+" +
                                        bufferSize + "+" +
                                        reporterAddress + "+" +
                                        reporterPort + "+" +
                                        reporterURI
                                        );
            
            reporterID = out.getString("createdReporterID"); 
        }

        catch (JSONException je) {
            System.err.println("Error while activating Reporter: " + reporterName);
        } 
        return reporterID;
    }
    
    
    void unloadReporter() throws Exception {
        restClient.unloadReporter(reporterID);
    }
    
    
    
    void createTopology() {
        try {
            dcHostSessionID = restClient.createSession(hostID, userID).getString("ID");
            dsHostSessionID = restClient.createSession(hostID, userID).getString("ID");
            
            startDataConsumer();
            loadReporter("buffered-reporter-" + topologyId);
            
            for (int i=0; i<dsNumber; i++)
                startDataSource();
                
            for (String dsID : dataSourceIDs) {
                System.out.println("Topology " + topologyId + ": Loading Probes / Sensors");
                
                List<String> probes = new ArrayList<>();
                dataSourceProbesIDs.put(dsID, probes);
                
                for (Integer i=0; i < nSensors; i++)
                    loadSensor(dsID, "Sensor-" + i, probes);

                System.out.println("Topology " + topologyId + ": Activating Probes / Sensors");
                for (String probeId : probes)
                    activateSensor(dsID, probeId);
            }
            
            System.out.println("*** Topology " + topologyId + " Created ***");
            
        } catch (Exception e) {
            System.err.println("Error while creating topology: " + this.topologyId + " – " + e.getMessage());
        }
    }
    
    
    void deleteTopology() {
        try {
            for (String dsID : dataSourceIDs)
                stopDataSource(dsID);
            
            unloadReporter();
            stopDataConsumer();
            
            restClient.deleteSession(dcHostSessionID);
            restClient.deleteSession(dsHostSessionID);
        } catch (Exception e) {
            System.err.println("Error while deleting topology: " + this.topologyId + " – " + e.getMessage());
        }
        
    }
    
    void startDeployment() {
        currentThread = new Thread( () -> this.createTopology() );
        currentThread.start();
    }
    
    void stopDeployment() {
        currentThread = new Thread( () -> this.deleteTopology() );
        currentThread.start();
    }
    
}
