package mon.lattice.appl.demo.iot;

import java.util.List;
import mon.lattice.appl.reporters.AbstractReporterWithInfoPlane;
import mon.lattice.control.im.ProbeAttributeNotFoundException;
import mon.lattice.control.im.ProbeNotFoundException;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Reporter for testing the retrieval of entries from the
 * Information Plane.
 */
public class LoggerReporterWithInfoPlane extends AbstractReporterWithInfoPlane {
    
    private Logger LOGGER = LoggerFactory.getLogger(LoggerReporterWithInfoPlane.class);
    
    public LoggerReporterWithInfoPlane(String name) {
        super(name);
    }

    @Override
    public void report(Measurement m) {
        
        try {
            ID probeID = m.getProbeID();
            String probename = reporterInformation.getProbeName(probeID);
            LOGGER.info(probename);
       
            List<ProbeValue> values = m.getValues();
            for (ProbeValue aValue : values)
                LOGGER.info(reporterInformation.getProbeAttributeName(probeID, aValue.getField()).toString());
                
            }
        catch (ProbeNotFoundException pe) {
            LOGGER.error("Error while retrieving probe Information" + pe.getMessage());   
        }
        
        catch (ProbeAttributeNotFoundException pae) {
            LOGGER.error("Error while retrieving probe attribute Information" + pae.getMessage());
                
        }
           
//	    String name = (String) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "name");
//	    String units = (String) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "units");
//            Integer t = (Integer) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "type");
//            
//            try {
//                ProbeAttributeType type = ProbeAttributeType.fromCode(t);
//                LOGGER.info(name + ": " + aValue.getValue() + " " + units + ", type " + type.toString());
//            } catch (TypeException e) {
//                LOGGER.error("Error while retrieving attribute type");
//            }
                 
    }    
}
