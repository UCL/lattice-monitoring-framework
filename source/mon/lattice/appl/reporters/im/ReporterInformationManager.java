/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.reporters.im;

import mon.lattice.core.ID;
import mon.lattice.core.plane.InfoPlane;
import java.util.HashMap;
import java.util.Map;
import mon.lattice.control.im.ProbeAttributeNotFoundException;
import mon.lattice.control.im.ProbeNotFoundException;

/**
 *
 * @author uceeftu
 */
public class ReporterInformationManager implements ReporterInformation {
    private final InfoPlane info;
    private final Map<ID,String> probesCache;
    private final Map<ID,String> probeAttributesCache;
    
    public ReporterInformationManager(InfoPlane info) {
        this.info = info;
        probesCache = new HashMap<>();
        probeAttributesCache = new HashMap<>();
    }
    
    @Override
    public String getProbeName(ID probeID) throws ProbeNotFoundException {
        String probeName = null;
        
        if (!probesCache.containsKey(probeID)) {
            probeName = (String)info.lookupProbeInfo(probeID, "name");
            probesCache.put(probeID, probeName);
        }
        else
            probeName = probesCache.get(probeID);

        if (probeName == null)
            throw new ProbeNotFoundException("Error while getting information for probe: " + probeID);
        
        return probeName;
    }
    
    @Override
    public Object getProbeAttributeName(ID probeID, int field) throws ProbeAttributeNotFoundException{
       String probeAttributeName = null;
       
       if (!probeAttributesCache.containsKey(probeID)) {
           probeAttributeName = (String)info.lookupProbeAttributeInfo(probeID, field, "name");
           probeAttributesCache.put(probeID, probeAttributeName);
       }
       else
           probeAttributeName = probeAttributesCache.get(probeID);
       
       if (probeAttributeName == null)
           throw new ProbeAttributeNotFoundException("Error while getting information for probe attribute: " + field + ", probe: " + probeID);
       
       return probeAttributeName;
    }
    
}
