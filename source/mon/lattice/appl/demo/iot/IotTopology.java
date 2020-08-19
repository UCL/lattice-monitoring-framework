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

    Integer reporterBufferSize;
    Integer reporterResponseTime;
    String reporterClassName;
    String reporterAddress;
    String reporterPort;
    String reporterURI;
    
    String reporterCallbackHost;
    String reporterCallbackPort;
    String reporterCallbackURI;
    
    Integer topologies;

    String dataConsumerID;
    String dsClassName;
    String dcDataplaneAddress;
    Integer dcDataPlanePort;
    String dsHostSessionID;
    
    String dcRemoteForwardingHost;
    Integer dcRemoteForwardingPort;
    
    List<String> dataSourceIDs = new ArrayList<>();
    String dcClassName;
    String dcHostSessionID; 
    
    String reporterID;
    Map<String, List<String>> dataSourceProbesIDs = new HashMap<>();
    
    Thread currentThread;

    ReporterLoader reporterLoader;
    
    Exception error;

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
        this.controllerAddress = controllerAddress;
        this.restClient = new RestClient(controllerAddress, controllerPort);
       }
    
    
    public void loadConfiguration() {
        controllerInfoPlanePort = configuration.getProperty("controller.infoplane.port");
        controllerControlPlanePort = configuration.getProperty("controller.controlplane.port");
        
        dcDataplaneAddress = configuration.getProperty("host.address");
        dsClassName = configuration.getProperty("ds.class");
        dcClassName = configuration.getProperty("dc.class");
        reporterClassName = configuration.getProperty("rep.class");
        
        // Loading reporter parameters is delegated to LoadReporter class
        reporterLoader = new ReporterLoader(reporterClassName);
        
        // in order to allocate a different Data Consumer reporterPort for each topology 
        // we add the topology ID to the value read from the conf file
        dcDataPlanePort = Integer.valueOf(configuration.getProperty("dc.dataplane.port")) + topologyId;
        
        if (dcClassName.contains("Forwarder")) {
            dcRemoteForwardingHost = configuration.getProperty("dc.dataplane.forward.host");
            dcRemoteForwardingPort = Integer.valueOf(configuration.getProperty("dc.dataplane.forward.port"));
        }
        
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
                                                                                          controllerControlPlanePort);
            
            if (startDS.has("ID"))
                dataSourceIDs.add(startDS.getString("ID"));
            else
                throw new Exception("Error while instantiating Data Source: " + startDS.getString("msg"));
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating Data Source: " + e.getMessage());
        }
    }
    
    
    private void startDataConsumer() throws Exception { 
        System.out.println("Deploying Data Consumer on host: " + hostID);
        
        try {
             JSONObject startDC = null;
            
            // checking if the DC is a forwarder
            if (dcClassName.contains("Forwarder")) {
                startDC = restClient.startDataConsumer(dcClassName, dcHostSessionID, dcDataPlanePort + "+" +
                                                                                     controllerAddress + "+" +
                                                                                     controllerInfoPlanePort + "+" +
                                                                                     controllerControlPlanePort + "+" +
                                                                                     dcRemoteForwardingHost + "+" +
                                                                                     dcRemoteForwardingPort);
            }
            
            else {
                startDC = restClient.startDataConsumer(dcClassName, dcHostSessionID, dcDataPlanePort + "+" +
                                                                                     controllerAddress + "+" +
                                                                                     controllerInfoPlanePort + "+" +
                                                                                     controllerControlPlanePort);
            }
            
            if (startDC != null && startDC.has("ID"))
                dataConsumerID = startDC.getString("ID");
            else
                throw new Exception("Error while instantiating Data Consumer: " + startDC.getString("msg"));
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating Data Consumer: " + e.getMessage());
        }
    }
        
    
    private void stopDataSource(String dsID) throws Exception {
        JSONObject out;
        System.out.println("Stopping Data Source on host: "  + hostID + " - DS id: " + dsID);
        try {
            if (dsID != null && dsHostSessionID != null) {
                out = restClient.stopDataSource(dsID, dsHostSessionID);
                
                if (!out.getBoolean("success"))
                    throw new Exception("Error while stopping Data Source: " + dsID + ": " + out.getString("msg"));
            }
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading Data Source: " + e.getMessage());
        }
    }
   
    
    private void stopDataConsumer() throws Exception {
        JSONObject out;
        
        try {
            if (dataConsumerID != null && dcHostSessionID != null) {
                System.out.println("Stopping Data Consumer on host: "  + hostID + " - DC id: " + dataConsumerID);
                out = restClient.stopDataConsumer(dataConsumerID, dcHostSessionID);  
    
                if (!out.getBoolean("success"))
                    throw new Exception("Error while stopping Data Consumer: " + dataConsumerID + ": " + out.getString("msg"));
            }
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading Data Consumer: " + e.getMessage());
        }
    }
    
    
    private void loadSensor(String dataSourceID, String probeName, List<String> probes)  {
        String probeClassName = "mon.lattice.appl.demo.iot.SensorEmulatorProbe";
        String probeAttributeName = "Temperature";
        String units = "Celsius";
        
        Integer value = ThreadLocalRandom.current().nextInt(valueMin, valueMax);
        try {
            JSONObject out = restClient.loadProbe(dataSourceID, 
                                                  probeClassName, 
                                                  probeName + "+" + probeAttributeName + "+" + value + "+" + rate + "+" + units + "+" + waitMin + "+" + waitMax
                                                 );
            
            if (out.has("createdProbeID")) {
                String probeID = out.getString("createdProbeID");
                probes.add(probeID);
            } else
                System.err.println("Topology " + topologyId + ": Error while loading Probe: " + probeName + " on DataSource " + dataSourceID + "\n" +
                                    out.getString("msg"));
            
        } catch (JSONException je) {
            System.err.println("Topology " + topologyId + ": Error while loading Probe: " + probeName + " on DataSource " + dataSourceID);
        }
    }
    
    
    private void activateSensor(String dataSourceID, String probeID) {
        try {
            restClient.setProbeServiceID(probeID, getRandomEntityID());
            restClient.turnOnProbe(probeID);
        } catch (JSONException je) {
            System.err.println("Topology " + topologyId + ": Error while activating Probe: " + probeID + " on DataSource " + dataSourceID);
        }
    }
    
    
    private String loadReporter(String reporterName) throws IOException {
        reporterLoader.parseName();
        return reporterLoader.loadReporter(reporterName);
    }
    
    
    void unloadReporter() throws Exception {
        System.out.println(restClient.unloadReporter(reporterID));
    }
    
    
    
    void createTopology() {
        try {
            // starting Data Consumers first
            JSONObject dcSession = restClient.createSession(hostID, userID);
            
            if (dcSession.has("ID"))
                dcHostSessionID = dcSession.getString("ID");
            else
                throw new Exception("Data Consumer error: " + dcSession.getString("msg"));
            
            startDataConsumer();
            reporterID = loadReporter("reporter-" + topologyId);
            
            // starting Data Sources now
            JSONObject dsSession = restClient.createSession(hostID, userID);
            
            if (dsSession.has("ID"))
                dsHostSessionID = dsSession.getString("ID");
            else
                throw new Exception("Data Source error: " + dsSession.getString("msg"));
            
            for (int i=0; i<dsNumber; i++)
                startDataSource();
            
        } catch (Exception e) {
            error = new Exception("Fatal error while creating topology: " + this.topologyId + " – " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }
        
        
        // emulation can continue now: errors on sensor loading are not fatal
        try {
            // TODO: should create multiple thread here
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
            
            if (dcHostSessionID != null) restClient.deleteSession(dcHostSessionID);
            if (dsHostSessionID != null) restClient.deleteSession(dsHostSessionID);
            
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
    
    
    
    class ReporterLoader {
        
        String fqClassName;
        String className;
        
        public ReporterLoader(String name) {
            fqClassName = name;
        }
        
        public void parseName() {
            String[] fqClassNameTokens = fqClassName.split("\\.");
            className = fqClassNameTokens[fqClassNameTokens.length-1];
        }
        
        private void setDestinationParams() {
            reporterAddress = configuration.getProperty("rep.address");
            reporterPort = configuration.getProperty("rep.port");
        }
        
        private void setCallbackParams() {
            reporterCallbackHost = configuration.getProperty("rep.callback.host");
            reporterCallbackPort = configuration.getProperty("rep.callback.port");
            reporterCallbackURI = configuration.getProperty("rep.callback.uri");
        }
       
        private String startReporter(String... args) throws IOException {
            boolean isAargNull = false;
            try {
                StringBuilder arguments = new StringBuilder();
                int i;
                for (i=0; i<args.length-1; i++) {
                    if (args[i] != null) {
                        arguments.append(args[i]);
                        arguments.append("+"); 
                    } 
                    
                    else {
                        isAargNull = true;
                        break;
                    }
                        
                }
                
                if (args[i] != null)
                    arguments.append(args[i]);
                else
                    isAargNull = true;
                            
                if (isAargNull)
                    throw new IOException("Error while activating Reporter: received a null arg");

                JSONObject reporter = restClient.loadReporter(dataConsumerID, fqClassName, arguments.toString());
                return reporter.getString("createdReporterID");
            } catch (JSONException e) {
                throw new IOException("Error while activating Reporter " + className + ": " + e.getMessage());
            }
        }
        
        
        public String loadReporter(String reporterName) throws IOException {
            
            switch(className) {
                /* JSON REST Reporters */
                case "BufferedJSONRestReporterWithCallback":
                    setDestinationParams();
                    setCallbackParams();
                    reporterURI = configuration.getProperty("rep.uri");
                    reporterBufferSize = Integer.valueOf(configuration.getProperty("rep.buffersize"));

                    return startReporter(reporterName, 
                                        reporterBufferSize.toString(), 
                                        reporterAddress, 
                                        reporterPort,
                                        reporterURI,
                                        reporterCallbackHost,
                                        reporterCallbackPort,
                                        reporterCallbackURI);
                    
                case "BufferedJSONRestReporter":
                case "BufferedRestReporter": // will be removed  
                    setDestinationParams();
                    reporterURI = configuration.getProperty("rep.uri");
                    reporterBufferSize = Integer.valueOf(configuration.getProperty("rep.buffersize"));
                    
                    return startReporter(reporterName, 
                                        reporterBufferSize.toString(), 
                                        reporterAddress, 
                                        reporterPort,
                                        reporterURI);
                    
                case "JSONRestReporter":
                    setDestinationParams();
                    reporterURI = configuration.getProperty("rep.uri");
                    
                    return startReporter(reporterName,
                                        reporterAddress, 
                                        reporterPort,
                                        reporterURI);
                    
                /* JSON WebSocket Reporters */    
                case "BufferedJSONWebSocketReporter":
                    setDestinationParams();
                    reporterBufferSize = Integer.valueOf(configuration.getProperty("rep.buffersize"));
                    
                    return startReporter(reporterName, 
                                        reporterBufferSize.toString(), 
                                        reporterAddress, 
                                        reporterPort);   
                    
                    
                case "JSONWebSocketReporter":
                case "XDRWebSocketReporter":
                case "XDRWebSocketReporterWithNames":        
                    setDestinationParams();
                    
                    return startReporter(reporterName,
                                        reporterAddress, 
                                        reporterPort);
                    
                
                /* Other Reporters */  
                case "EmulatedReportTimeReporter":
                    reporterResponseTime = Integer.valueOf(configuration.getProperty("rep.response"));
                    return startReporter(reporterName,
                                  reporterResponseTime.toString());
                    
                case "VoidReporter":
                    return startReporter(reporterName);
                    
                default:
                    throw new IOException("Reporter class: " + fqClassName + " is not supported");
            }        
        }
    }

    
    public Exception getError() {
        return error;
    }
    
}
