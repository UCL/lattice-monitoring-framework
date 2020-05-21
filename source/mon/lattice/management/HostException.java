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
public class HostException extends Exception {

    /**
     * Creates a new instance of <code>HostException</code> without detail
     * message.
     */
    public HostException() {
    }

    /**
     * Constructs an instance of <code>HostException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public HostException(String msg) {
        super(msg);
    }
}
