/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management;

import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public class User {
    String username;
    ID id;
    
    public User(String username) {
        this.username = username;
        this.id = ID.generate();
    }

    public String getUsername() {
        return username;
    }

    public ID getId() {
        return id;
    }
}
