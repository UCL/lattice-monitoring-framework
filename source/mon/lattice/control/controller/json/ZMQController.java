package mon.lattice.control.controller.json;

import mon.lattice.core.plane.InfoPlane;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import mon.lattice.control.zmq.ZMQControlPlaneXDRProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.im.zmq.ZMQControllerInfoPlane;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.control.im.ControlInformationInteracter;

/**
 *
 * @author uceeftu
 */
public class ZMQController extends AbstractJSONRestController {
    
    private static final ZMQController CONTROLLER = new ZMQController();
    
    private static Logger LOGGER;
    

    protected ZMQController() {}
     
    
    @Override
    public void initPlanes() {
        controlLocalPort = Integer.parseInt(pr.getProperty("control.localport"));
        infoPlanePort = Integer.parseInt(pr.getProperty("info.localport"));
        infoPoolSize = Integer.parseInt(pr.getProperty("info.poolsize"));
        
        // ZMQController is the root of the infoPlane - other nodes use it to perform bootstrap
        InfoPlane infoPlane;
        if (infoPoolSize == 0)
            infoPlane = new ZMQControllerInfoPlane(infoPlanePort);
        else
            infoPlane = new ZMQControllerInfoPlane(infoPlanePort, infoPoolSize);
        
        // we get the ControlInformationManager from the InfoPlane
        controlInformationManager = ((ControlInformationInteracter) infoPlane).getControlInformation();
        
	setInfoPlane(infoPlane);
        
        // create a ZMQ control plane producer
        ControlPlane controlPlane = new ZMQControlPlaneXDRProducer(controlLocalPort);
        
        // setting a reference to the ControlInformation on the Control Plane
        ((ControlInformationInteracter) controlPlane).setControlInformation(controlInformationManager);
        setControlPlane(controlPlane);
        
        connect();
    }
    
    
    public static ZMQController getInstance() {
        return CONTROLLER;
    }
    
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(ZMQController.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/ZMQController.properties";
                break;
            case 1:
                propertiesFile = args[0];
                break;
            default:
                LOGGER.error("Controller main: please use: java Controller [file.properties]");
                System.exit(1);
        }
        
	try {
            // loading properties file
            input = new FileInputStream(propertiesFile);
            prop.load(input);
            
	} catch (Exception ex) {
		LOGGER.error("Error while opening the property file: " + ex.getMessage());
                LOGGER.error("Falling back to default configuration values");
	} finally {        
            if (input != null) {
                try {
                    input.close();
                    } catch (IOException e) {        
                    }
            }
        }
        
        ZMQController myController = ZMQController.getInstance();
        myController.setPropertyHandler(prop);
        myController.initPlanes();
        myController.init();
        myController.initRESTConsole();

    }
}