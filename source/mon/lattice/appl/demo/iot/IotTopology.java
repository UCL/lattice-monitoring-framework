/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.demo.iot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class IotTopology {
    RestInteractor restClient;
    List<String> entityIDs;
    Integer topologyId;
    
    Integer nSensors;
    Integer rate;
    Integer waitMin;
    Integer waitMax;
    Integer valueMin;
    Integer valueMax;

    Integer bufferSize;
    String address;
    String port;
    String uri;
    Integer topologies;

    String dataConsumerID;
    String dataSourceID;
    String reporterID;
    List<String> probesIDs = new ArrayList<>();
    
    Thread currentThread;
    

    public IotTopology(int topologyId,
                       int nSensors,
                       int rate,
                       int waitMin,
                       int waitMax,
                       int valueMin,
                       int valueMax,
                       int bufferSize,
                       String address,
                       String port,
                       String uri,
                       RestInteractor r) 
       {
        this.topologyId = topologyId;
        this.nSensors = nSensors;
        this.rate = rate;
        this.waitMin = waitMin;
        this.waitMax = waitMax;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.bufferSize = bufferSize;
        this.address = address;
        this.port = port;
        this.uri = uri;   
        this.restClient = r;
       }
    
    
    public void setEntitiesIDs(List l) {
        this.entityIDs = l;
    }
    
    
    private String getRandomEntityID() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, entityIDs.size());
        return entityIDs.get(randomIndex);
    }
    
    
    private void deployDS() throws Exception {
        dataSourceID = restClient.instantiateDS();
    }
    
    
    private void deployDC() throws Exception {
        dataConsumerID = restClient.instantiateDC();
    }
    
    
    private void unDeployDS() throws Exception {
        restClient.unloadDS(dataSourceID);
    }
   
    
    private void unDeployDC () throws Exception {
        restClient.unloadDC(dataConsumerID);
    }
    
    
    private void loadSensor(String probeName)  {
        String probeClassName = "mon.lattice.appl.demo.SensorEmulatorProbe";
        String probeAttributeName = "temperature";
        
        Integer value = ThreadLocalRandom.current().nextInt(valueMin, valueMax);
        try {
            JSONObject out = restClient.loadProbe(dataSourceID, probeClassName, probeName + "+" + probeAttributeName + "+" + value + "+" + rate);
            String probeID = out.getString("createdProbeID");
            probesIDs.add(probeID);
        } catch (JSONException je) {
            System.err.println("Topology " + topologyId + ": Error while loading Probe: " + probeName + " on DataSource " + dataSourceID);
        }
    }
    
    
    private void activateSensor(String probeID)  throws InterruptedException {
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
                                        address + "+" +
                                        port + "+" +
                                        uri
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
            //changing the initial DC port setting according to the topology ID
            //this might need to be done differently
            Integer dcPort = Integer.valueOf(restClient.getDCDataPlanePort());
            dcPort += topologyId;
            restClient.setDCDataPlanePort(dcPort.toString());
            
            deployDC();
            loadReporter("buffered-reporter-" + topologyId);
            
            deployDS();
            System.out.println("Topology " + topologyId + ": Loading Probes / Sensors");
            for (Integer i=0; i < nSensors; i++)
                loadSensor("Sensor-" + i);
            
            System.out.println("Topology " + topologyId + ": Activating Probes / Sensors");
            for (String probeId : probesIDs)
                activateSensor(probeId);
            
            System.out.println("*** Topology " + topologyId + " Created ***");
            
        } catch (Exception e) {
            System.err.println("Error while creating topology: " + this.topologyId + " – " + e.getMessage());
        }
        
    }
    
    
    void deleteTopology() {
        try {
            this.unDeployDS();
            this.unloadReporter();
            this.unDeployDC();
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
