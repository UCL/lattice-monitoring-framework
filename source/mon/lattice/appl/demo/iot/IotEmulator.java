package mon.lattice.appl.demo.iot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import mon.lattice.appl.RestClient;
import us.monoid.json.JSONException;


public class IotEmulator {
    RestClient restClient;
    Properties configuration;
    
    String username;
    String userKey;
    
    String hostAddress;
    Integer hostPort;
    
    String controllerAddress;
    Integer controllerPort;
    
    String userID;
    String hostID;
    
    Integer topologies;
    List<String> entityIDs = new ArrayList<>();
    
    Integer nSensors;
    Integer nEntities;
    Integer rate;
    Integer waitMin;
    Integer waitMax;
    Integer valueMin;
    Integer valueMax;
    Integer dsNumber;

    
    public IotEmulator(Properties configuration) {
        this.configuration = configuration;
    }

    void loadEmulationConfiguration() {
        username = configuration.getProperty("user.id");
        userKey = configuration.getProperty("user.key");
        
        // we use the same host for Data Sources and Data Consumers
        hostAddress = configuration.getProperty("host.address");
        hostPort = Integer.valueOf(configuration.getProperty("host.port"));
        
        controllerAddress = configuration.getProperty("controller.address");
        controllerPort = Integer.valueOf(configuration.getProperty("controller.rest.port"));
        
        nSensors = Integer.valueOf(configuration.getProperty("sensors.number"));
        nEntities = Integer.valueOf(configuration.getProperty("entities.number"));
        rate = Integer.valueOf(configuration.getProperty("probe.rate", "2000"));
        waitMin = Integer.valueOf(configuration.getProperty("probe.activation.min", "100"));
        waitMax = Integer.valueOf(configuration.getProperty("probe.activation.max", "200"));
        valueMin = Integer.valueOf(configuration.getProperty("probe.value.min", "10"));
        valueMax = Integer.valueOf(configuration.getProperty("probe.value.max", "40"));
        topologies = Integer.valueOf(configuration.getProperty("topologies.number", "1"));
        dsNumber = Integer.valueOf(configuration.getProperty("ds.number"));
    }
    
    
    void printEmulationConfiguration() {
        System.out.println("*** Using the following Configuration ***");
        System.out.println("Number of emulated monitored Entities: " + nEntities);
        System.out.println("Number of Data Sources per topology: " + dsNumber);
        System.out.println("Number of Probes/Sensors per Data Source: " + nSensors);
        System.out.println("Probes/Sensors rate: " + rate);
        System.out.println("Probes/Sensors random activation interval: " + waitMin + "-" + waitMax);
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
    
    void initialise() throws IOException, JSONException {
        restClient = new RestClient(controllerAddress, controllerPort);
        userID = restClient.addUser(username, "KEY", userKey).getString("ID");
            
        // we create a single host object as it is the same for all the Data Sources and Data Consumers
        hostID = restClient.addHost(hostAddress, hostPort.toString()).getString("ID");
    }
    
    
    void cleanup() {
        try {
            restClient.removeHost(hostID);
            restClient.deleteUser(userID);
            } catch (Exception e) {
                System.err.println("There was an error while removing either the users or the hosts" + e.getMessage());
            }
    }
    
    
    public static void main(String[] args) {
        IotEmulator iot = null;
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
            iot.initialise();
            
            // creating requested sensors topologies
            for (int id=1; id <= iot.topologies; id++) {
                System.out.println("*** Creating Topology " + id + " ***");
                
                IotTopology t = new IotTopology(id,
                                                iot.userID,
                                                iot.hostID,
                                                iot.dsNumber,
                                                iot.nSensors,
                                                iot.rate,
                                                iot.waitMin,
                                                iot.waitMax,
                                                iot.valueMin,
                                                iot.valueMax,
                                                iot.controllerAddress,
                                                iot.controllerPort
                                                );
                
                t.setProperties(configuration);
                t.loadConfiguration();
                t.setEntitiesIDs(iot.entityIDs);
                iotList.add(t);
                t.startDeployment();

            }
            
            for (IotTopology t : iotList)
                t.currentThread.join();
            
            System.out.print("\n*** Deployment Completed ***\n");
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
            
            if (iot != null) 
                iot.cleanup();
            
        }
    if (errorStatus)
        System.exit(1);
    }
}
