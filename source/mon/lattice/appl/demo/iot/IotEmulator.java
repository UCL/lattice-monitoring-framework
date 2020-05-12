/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.demo.iot;

import eu.fivegex.monitoring.test.LatticeTest;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class IotEmulator {
    Properties configuration;
    
    Integer topologies;
    LatticeTest restClient;
    List<String> entityIDs = new ArrayList<>();
    
    Integer nSensors;
    Integer nEntities;
    Integer rate;
    Integer waitMin;
    Integer waitMax;
    Integer valueMin;
    Integer valueMax;

    Integer bufferSize;
    String address;
    String port;
    String uri;

    
    public IotEmulator(Properties configuration) {
        this.configuration = configuration;
    }

    void loadEmulationConfiguration() {
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
        rate = Integer.valueOf(configuration.getProperty("probe.rate", "2000"));
        waitMin = Integer.valueOf(configuration.getProperty("probe.activation.min", "100"));
        waitMax = Integer.valueOf(configuration.getProperty("probe.activation.max", "200"));
        valueMin = Integer.valueOf(configuration.getProperty("probe.value.min", "10"));
        valueMax = Integer.valueOf(configuration.getProperty("probe.value.max", "40"));
        topologies = Integer.valueOf(configuration.getProperty("topologies.number", "1"));
        bufferSize = Integer.valueOf(configuration.getProperty("rep.buffersize"));
        address = configuration.getProperty("rep.address");
        port = configuration.getProperty("rep.port");
        uri = configuration.getProperty("rep.uri");
    }
    
    
    void printEmulationConfiguration() {
        System.out.println("*** Using the following Configuration ***");
        System.out.println("Number of emulated monitored Entities: " + nEntities);
        System.out.println("Number of Probes/Sensors: " + nSensors);
        System.out.println("Probes/Sensors rate: " + rate);
        System.out.println("Probes/Sensors random activation interval: " + waitMin + "-" + waitMax);
        System.out.println("Reporter buffer size: " + bufferSize);
        System.out.println("Reporter destination URL: " + "http://" + address + ":" + port + "/" + uri);
        System.out.println("Number of concurrent generators: " + topologies);
        System.out.println();
    }
    
    void generateEntityIDs() {
        for (int i=0; i < nEntities; i++)
            entityIDs.add(UUID.randomUUID().toString());
    }
    
    
    String getRandomEntityID() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, nEntities);
        return entityIDs.get(randomIndex);
    }
    
    
    
    public static void main(String[] args) {
        IotEmulator iot;
        List<IotTopology> iotList = new ArrayList<>();
        
        boolean errorStatus = false;
        
        try {
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
                    System.out.println("Please use: java IotEmulator [file.properties]");
                    System.exit(1);
            }
            
            // initialising global emulation settings
            input = new FileInputStream(propertiesFile);
            Properties configuration = new Properties();
            configuration.load(input);
            
            iot = new IotEmulator(configuration);
            
            iot.loadEmulationConfiguration();
            iot.printEmulationConfiguration();
            iot.generateEntityIDs();
            
            // creating requested sensors topologies
            for (int id=1; id <= iot.topologies; id++) {
                System.out.println("*** Creating Topology " + id + " ***");
                
                //creating a RestInteractor for this topology
                RestInteractor r = new RestInteractor(configuration);
                
                IotTopology t = new IotTopology(id,
                                                iot.nSensors,
                                                iot.rate,
                                                iot.waitMin,
                                                iot.waitMax,
                                                iot.valueMin,
                                                iot.valueMax,
                                                iot.bufferSize,
                                                iot.address,
                                                iot.port,
                                                iot.uri,
                                                r);
                
                t.setEntitiesIDs(iot.entityIDs);
                iotList.add(t);
                t.startDeployment();

            }
            
            for (IotTopology t : iotList)
                t.currentThread.join();
            
            System.out.printf("\n*** Deployment Completed ***\n");
            System.out.print("\nPress a key to stop the emulation");
            System.in.read();
        }
        
        catch (Exception e) {
            System.out.println("\n*** DEPLOYMENT FAILED ***\n" + e.getMessage());
            errorStatus = true;
        }
        
        finally {
            // stopping the emulation
            for (IotTopology t : iotList) {
                t.stopDeployment();
            }
        }
    if (errorStatus)
        System.exit(1);
    }
}
