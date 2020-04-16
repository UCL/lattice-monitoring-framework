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
    Properties configuration;
    Integer nSensors;
    Integer nEntities;
    
    List<String> IDs = new ArrayList<>();;
   
    
    public IoTScenario(Properties configuration) throws UnknownHostException, IOException {
        this.configuration = configuration;
        restClient = new LatticeTest(configuration);
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
    }

    
    private void generateEntityIDs() {
        for (int i=0; i < nEntities; i++)
            IDs.add(UUID.randomUUID().toString());
    }
    
    
    private String getEntityID() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, nEntities);
        return IDs.get(randomIndex);
    }
    
    
    private void loadSensor(String dsID, String probeName, String probeAttributeName, String value, String entityId) throws JSONException {
        String probeClassName = "mon.lattice.appl.demo.SensorEmulatorProbe";
        
        JSONObject out = restClient.loadProbe(dsID, probeClassName, probeName + "+" + probeAttributeName + "+" + value);
        String probeID = out.getString("createdProbeID");
        
        restClient.setProbeServiceID(probeID, entityId);
        restClient.turnOnProbe(probeID);
    }
    
    
    private String loadReporter(String dcID, String reporterName) throws Exception {
        String reporterClassName = "mon.lattice.appl.reporters.BufferedRestReporter";
        String bufferSize = configuration.getProperty("rep.buffersize");
        String address = configuration.getProperty("rep.address");
        String port = configuration.getProperty("rep.port");
        String uri = configuration.getProperty("rep.uri");
        
        JSONObject out;
        
        try {
            System.out.println("Starting reporter on DC: " + dcID);
            System.out.println("Dinamically loading reporter class: " + reporterClassName);

            out = restClient.loadReporter(dcID, reporterClassName, 
                                        reporterName + "+" +
                                        bufferSize + "+" +
                                        address + "+" +
                                        port + "+" +
                                        uri
                                        );
            
            String reporterID = out.getString("createdReporterID");  
            return reporterID;
        }

        catch (JSONException ex) {
            throw new Exception("Test Case loadReporter Failed! " + "\nReason: " + ex.getMessage());
        } 
    }
    
    
    
    
    public static void main(String[] args) {
        IoTScenario iot = null;
        String dsID = null;
        String dcID = null;
        String reporterID = null;
        
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
            
            iot = new IoTScenario(configuration);

            // instantiating a new DS on the endpoint as per configuration (field DSEndPointAddress)
            dsID = iot.restClient.instantiateDS();
            
            dcID = iot.restClient.instantiateDC();
            
            iot.dataConsumerID = dcID;
            iot.dataSourceID= dsID;
            
            iot.generateEntityIDs();
            
            for (Integer i=0; i < iot.nSensors; i++) {
                //generating a random value for the probe
                Integer value = ThreadLocalRandom.current().nextInt(10, 40);
                iot.loadSensor(dsID, "Sensor" + i, "temperature", value.toString(), iot.getEntityID());
                Thread.sleep(500);
            }
            
            iot.loadReporter(dcID, "buffered-reporter");
            
            System.in.read();
            
        }
        
        catch (Exception e) {
            System.out.println("*DEPLOYMENT FAILED*\n" + e.getMessage());
            errorStatus = true;
        }
        finally {
            // trying to stop the previous instantiated DS/DC anyway
            try {
                if (iot.restClient != null) {
                    if (dsID != null)
                        iot.restClient.unloadDS(dsID);
                    if (dcID != null)  {
                        if (reporterID != null) {
                            System.out.println("Unloading Reporter " + reporterID);
                            iot.restClient.unloadReporter(reporterID);
                        }
                        iot.restClient.unloadDC(dcID);
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
