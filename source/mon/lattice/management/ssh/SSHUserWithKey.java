/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.ssh;

import java.io.File;
import mon.lattice.management.User;
import mon.lattice.management.UserException;

/**
 *
 * @author uceeftu
 */
public class SSHUserWithKey extends User {
    File identityFile;
    
    public SSHUserWithKey(String id, String identity) throws UserException {
        super(id);
        this.identityFile = new File(identity); 
        
        if (!identityFile.exists())
            throw new UserException("The provided key does not exist");
    }

    
    public File getIdentityFile() {
        return identityFile;
    }
    
    
}
