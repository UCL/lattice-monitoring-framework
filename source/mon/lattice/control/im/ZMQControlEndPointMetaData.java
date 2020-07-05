/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.im;

import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public class ZMQControlEndPointMetaData extends AbstractControlEndPointMetaData {
    private ID id;

    
    public ZMQControlEndPointMetaData(String type, ID id) {
        super(type);
        this.id = id;
    }

    public ID getId() {
        return id;
    }
}
