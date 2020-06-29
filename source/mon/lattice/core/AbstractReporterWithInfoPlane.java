/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

import mon.lattice.core.plane.InfoPlane;
import mon.lattice.core.plane.InfoPlaneInteracter;


public abstract class AbstractReporterWithInfoPlane extends AbstractReporter implements InfoPlaneInteracter {
    
    protected InfoPlane infoPlane;

    public AbstractReporterWithInfoPlane(String name) {
        super(name);
    }

    @Override
    public void setInfoPlane(InfoPlane infoplane) {
        this.infoPlane = infoplane;
    }

    @Override
    public InfoPlane getInfoPlane() {
        return this.infoPlane;
    }
    
    
    
}
