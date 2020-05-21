/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.management.ssh;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import mon.lattice.management.Host;

/**
 *
 * @author uceeftu
 */
public class SSHHost extends Host {    
    private static final Map<InetSocketAddress, SSHHost> hosts = new HashMap<>();
    
    
    
    public static synchronized SSHHost getInstance(String address, int port) {
        InetSocketAddress internalID = new InetSocketAddress(address, port);
        if (hosts.containsKey(internalID))
            return hosts.get(internalID); // this may return null to notify the rest call
        else {
            SSHHost host = new SSHHost(address, port);
            hosts.put(internalID, host);
            return host;
        }
    }
    
    
    public SSHHost(String address, int port) {
        super(address, port);
        
    }
    
}
