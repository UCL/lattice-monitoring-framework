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
public abstract class AbstractSession {
    protected ID id;
    protected final Host host;
    protected final User user;

    public AbstractSession(Host host, User user) {
        this.host = host;
        this.user = user;
    }
    
    public ID getId() {
        return id;
    }

    public Host getHost() {
        return host;
    }

    public User getUser() {
        return user;
    }
   
    
    public abstract void startEntity(LatticeEntityInfo entity) throws SessionException;
    
    public abstract void stopEntity(LatticeEntityInfo entity) throws SessionException;
    
}
