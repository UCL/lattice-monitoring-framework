package mon.lattice.appl.demo.iot;

import java.util.List;
import mon.lattice.core.AbstractReporterWithInfoPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.TypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Reporter for testing the retrieval of entries from the
 * Information Plane
 */
public class LoggerReporterWithInfoPlane extends AbstractReporterWithInfoPlane {
    
    private Logger LOGGER = LoggerFactory.getLogger(LoggerReporterWithInfoPlane.class);
    
    public LoggerReporterWithInfoPlane(String name) {
        super(name);
    }

    @Override
    public void report(Measurement m) {
        
        String probename = (String) infoPlane.lookupProbeInfo(m.getProbeID(), "name");
        LOGGER.info(probename);
       
        List<ProbeValue> values = m.getValues();
        for (ProbeValue aValue : values) {
	    String name = (String) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "name");
	    String units = (String) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "units");
            Integer t = (Integer) infoPlane.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "type");
            
            try {
                ProbeAttributeType type = ProbeAttributeType.fromCode(t);
                LOGGER.info(name + ": " + aValue.getValue() + " " + units + ", type " + type.toString());
            } catch (TypeException e) {
                LOGGER.error("Error while retrieving attribute type");
            }
            
            

	    
	}        
    }
    
    
    
}
