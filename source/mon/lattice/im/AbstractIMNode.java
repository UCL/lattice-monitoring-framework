package mon.lattice.im;

/**
 * @author Francesco Tusa
 * An AbstractIMNode extends the IMBasicNode by implementing
 * the method to get the information about the entry Point
 * of the Hierarchical information system, i.e. the remoteHost
 */
public abstract class AbstractIMNode implements IMBasicNode {
    
    protected String remoteHost;
    
    @Override
    public String getRemoteHostname() {
        return this.remoteHost;
    }
    
}
