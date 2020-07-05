package mon.lattice.appl.reporters.im;

import mon.lattice.control.im.ProbeAttributeNotFoundException;
import mon.lattice.control.im.ProbeNotFoundException;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface ReporterInformation {

    Object getProbeAttributeName(ID probeID, int field) throws ProbeAttributeNotFoundException;

    String getProbeName(ID probeID) throws ProbeNotFoundException;
    
}
