/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.reporters;

import mon.lattice.appl.reporters.im.ReporterInformation;
import mon.lattice.appl.reporters.im.ReporterInformationInteracter;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractReporterWithInfoPlane extends AbstractControllableReporter implements ReporterInformationInteracter {
        
    protected ReporterInformation reporterInformation;
    
    
    public AbstractReporterWithInfoPlane(String name) {
        super(name);
    }
    
    @Override
    public void setReporterInformation(ReporterInformation ri) {
        reporterInformation = ri;
    }
    
    @Override
    public ReporterInformation getReporterInformation() {
        return reporterInformation;
    }
    
    
    @Override
    public abstract void report(Measurement m);
    
}
