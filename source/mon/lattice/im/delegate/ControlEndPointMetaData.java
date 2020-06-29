/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

/**
 *
 * @author uceeftu
 */
public abstract class ControlEndPointMetaData implements ControlEndPointMetadataInterface {
    protected final String type;

    public ControlEndPointMetaData(String type) {
        this.type = type;
    }
    
    @Override
    public String getType() {
        return type;
    }  
}
