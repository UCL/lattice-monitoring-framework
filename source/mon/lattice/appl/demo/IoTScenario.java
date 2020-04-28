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
    
    LatticeTest restClient = null;
    String dataConsumerID;
    String dataSourceID;
    String reporterID;
    Properties configuration;
    Integer nSensors;
    Integer nEntities;
    
    List<String> IDs = new ArrayList<>();
    
    int scenarioId;
    
    
    public IoTScenario(Properties configuration) throws UnknownHostException, IOException {
        this.configuration = configuration;
        restClient = new LatticeTest(configuration);
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
    }
    
    
    public IoTScenario(Properties configuration, int scenarioId) throws UnknownHostException, IOException {
        this.configuration = configuration;
        restClient = new LatticeTest(configuration);
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
        this.scenarioId = scenarioId;
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
    
    
    private void loadSensor(String probeName, String probeAttributeName, String value, String entityId) throws JSONException {
        String probeClassName = "mon.lattice.appl.demo.SensorEmulatorProbe";
        
        JSONObject out = restClient.loadProbe(dataSourceID, probeClassName, probeName + "+" + probeAttributeName + "+" + value);
        String probeID = out.getString("createdProbeID");
        
        restClient.setProbeServiceID(probeID, entityId);
        restClient.turnOnProbe(probeID);
    }
    
    
    private String loadReporter(String reporterName) throws Exception {
        String reporterClassName = "mon.lattice.appl.reporters.BufferedRestReporter";
        String bufferSize = configuration.getProperty("rep.buffersize");
        String address = configuration.getProperty("rep.address");
        String port = configuration.getProperty("rep.port");
        String uri = configuration.getProperty("rep.uri");
        
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
            Properties configuration = new Properties();
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
            
            
            int topologies = Integer.valueOf(configuration.getProperty("topologies.number", "1"));
            
            
            for (int id=0; id < topologies; id++) {
                System.out.println("\n*** Deploying Topology " + id + " ***");
                IoTScenario iot = new IoTScenario(configuration, id);
                iot.deployDS();
                iot.deployDC();
                iot.generateEntityIDs();
            
                for (Integer i=0; i < iot.nSensors; i++) {
                    //generating a random value for the probe
                    Integer value = ThreadLocalRandom.current().nextInt(10, 40);
                    iot.loadSensor("Sensor" + i, "temperature", value.toString(), iot.getEntityID());
                    Thread.sleep(10);
                }
            
                iot.loadReporter("buffered-reporter");
                iotList.add(iot);
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