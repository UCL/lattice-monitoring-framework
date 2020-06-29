package mon.lattice.im;

/**
 * @author Francesco Tusa
 * An IMBasicNode has the functionalities to
 * connect and disconnect to / from a Hierarchical Distributed Information system 
 */
public interface IMBasicNode {
    /**
     * Connect to the Information System
     * @return true if successful
     */
    public boolean connect();

    /**
    * Disconnect from the Information System
    * @return true if successful
    */
    public boolean disconnect();  
    
    /**
    * Connect to the Information System
    * @return true if successful
    */
    public String getRemoteHostname();
}

