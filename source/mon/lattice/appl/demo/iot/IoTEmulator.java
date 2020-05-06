/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.demo;

import eu.fivegex.monitoring.test.LatticeTest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author uceeftu
 */
public class IoTScenario {
    static Properties configuration;
    static Integer nSensors;
    static Integer nEntities;
    static Integer rate;
    static Integer waitMin;
    static Integer waitMax;
    
    static String bufferSize;
    static String address;
    static String port;
    static String uri;
    static Integer topologies;
    
    LatticeTest restClient = null;
    
    String dataConsumerID;
    String dataSourceID;
    String reporterID;
    
    List<String> IDs = new ArrayList<>();
    
    int scenarioId;
    
    
        
    public IoTScenario() throws UnknownHostException, IOException {
        restClient = new LatticeTest(configuration);
    }
    
    
    public IoTScenario(int scenarioId) throws UnknownHostException, IOException {
        this();
        this.scenarioId = scenarioId;
    }
    

    static void loadEmulationConfiguration() {
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
        rate = Integer.valueOf(configuration.getProperty("probe.rate", "2000"));
        waitMin = Integer.valueOf(configuration.getProperty("probe.activation.min", "100"));
        waitMax = Integer.valueOf(configuration.getProperty("probe.activation.max", "200"));
        topologies = Integer.valueOf(configuration.getProperty("topologies.number", "1"));
        bufferSize = configuration.getProperty("rep.buffersize");
        address = configuration.getProperty("rep.address");
        port = configuration.getProperty("rep.port");
        uri = configuration.getProperty("rep.uri");
    }
    
    
    static void printEmulationConfiguration() {
        System.out.println("\n*** Using the following Configuration ***\n");
        System.out.println("Number of emulated monitored Entities: " + nEntities);
        System.out.println("Number of Probes/Sensors: " + nSensors);
        System.out.println("Probes/Sensors rate: " + rate);
        System.out.println("Probes/Sensors random activation interval: " + waitMin + "-" + waitMax);
        System.out.println("Reporter buffer size: " + bufferSize);
        System.out.println("Reporter destination URL: " + "http://" + address + ":" + port + "/" + uri);
        System.out.println("Number of concurrent generators: " + topologies);
        
    }
    
    private void generateEntityIDs() {
        for (int i=0; i < nEntities; i++)
            IDs.add(UUID.randomUUID().toString());
    }
    
    
    private String getEntityID() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, nEntities);
        return IDs.get(randomIndex);
    }
    
    
    private void deployDS() throws Exception {
        dataSourceID = restClient.instantiateDS();
    }
    
    
    private void deployDC() throws Exception {
        Integer dcPort = Integer.valueOf(restClient.getDCDataPlanePort());
        dcPort += scenarioId;
        restClient.setDCDataPlanePort(dcPort.toString());
        dataConsumerID = restClient.instantiateDC();
    }
    
    
    private void unDeployDS() throws Exception {
        restClient.unloadDS(dataSourceID);
    }
   
    
    private void unDeployDC () throws Exception {
        restClient.unloadDC(dataConsumerID);
    }
    
    
    private void loadSensor(String probeName, String probeAttributeName, String value, String entityId) throws JSONException, InterruptedException  {
        String probeClassName = "mon.lattice.appl.demo.SensorEmulatorProbe";
        
        JSONObject out = restClient.loadProbe(dataSourceID, probeClassName, probeName + "+" + probeAttributeName + "+" + value + "+" + rate);
        String probeID = out.getString("createdProbeID");
        
        // waiting a random time in msecs before activating the sensor / probe
        int randomWait = ThreadLocalRandom.current().nextInt(waitMin, waitMax);
        Thread.sleep(randomWait);
        restClient.setProbeServiceID(probeID, entityId);
        restClient.turnOnProbe(probeID);
    }
    
    
    private String loadReporter(String reporterName) throws Exception {
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
            return reporterID;
        }

        catch (JSONException ex) {
            throw new Exception("Test Case loadReporter Failed! " + "\nReason: " + ex.getMessage());
        } 
    }
    
    
    private void unloadReporter() throws Exception {
        restClient.unloadReporter(reporterID);
    }
    
    
    
    
    public static void main(String[] args) {
        List<IoTScenario> iotList = new ArrayList<>();
        
        boolean errorStatus = false;
        
        try {
            configuration = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            switch (args.length) {
                case 0:
                    propertiesFile = System.getProperty("user.home") + "/iot.properties";
                    break;
                case 1:
                    propertiesFile = args[0];
                    break;
                default:
                    System.out.println("Please use: java IotScenario [file.properties]");
                    System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            configuration.load(input);
            loadEmulationConfiguration();
            printEmulationConfiguration();
            
            for (int id=1; id <= topologies; id++) {
                System.out.println("\n*** Deploying Topology " + id + " ***");
                IoTScenario iot = new IoTScenario(id);
                iotList.add(iot);
                
                iot.deployDC();
                iot.generateEntityIDs();
                
                iot.deployDS();
                System.out.println("Loading Probes / Sensors");
            
                for (Integer i=0; i < nSensors; i++) {
                    //generating a random value for the probe
                    Integer value = ThreadLocalRandom.current().nextInt(10, 40);
                    iot.loadSensor("Sensor" + i, "temperature", value.toString(), iot.getEntityID());
                }
            
                iot.loadReporter("buffered-reporter");
            }
            
            System.out.printf("\n*** Deployment Completed ***\n");
            System.out.print("\nPress a key to stop the emulation");
            System.in.read();
        }
        
        catch (Exception e) {
            System.out.println("\n*** DEPLOYMENT FAILED ***\n" + e.getMessage());
            errorStatus = true;
        }
        
        finally {
            // stopping the emulation (in any case)
            
            for (IoTScenario iot : iotList) {
                try {
                    iot.unDeployDS();
                    iot.unloadReporter();
                    iot.unDeployDC();
                }
                catch (Exception e) { // the DS/DC was either already stopped or not running
                }
            }
        }
    if (errorStatus)
        System.exit(1);
    }
}