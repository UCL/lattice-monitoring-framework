/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management;

/**
 *
 * @author uceeftu
 */
public class ManagementException extends Exception {
    
    public ManagementException(Exception e) {
        super(e);
    }
    
    public ManagementException (String msg) {
        super(msg);
    }
    
}
