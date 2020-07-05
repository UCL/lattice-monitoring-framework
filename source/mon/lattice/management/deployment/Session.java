/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.deployment;

import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface Session {

    Host getHost();

    ID getId();

    User getUser();

    void startEntity(LatticeEntityInfo entity) throws SessionException;

    void stopEntity(LatticeEntityInfo entity) throws SessionException;
    
}
