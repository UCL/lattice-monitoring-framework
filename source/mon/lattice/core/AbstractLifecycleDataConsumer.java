package mon.lattice.core;

/**
 * This abstract Data Consumer has a shutdown hook.
 * A thread executing the content of the cleanup() method is passed to that hook
 * when a termination signal is received by the process.
 * 
 * @author uceeftu
 */
public class AbstractLifecycleDataConsumer extends AbstractDataConsumer {
    
    public AbstractLifecycleDataConsumer() {
            Runtime.getRuntime().addShutdownHook(new Thread( () -> cleanup()));

    }
    
    
    private void cleanup() {
        System.out.println("Shutting Down");
        super.disconnect(); 
    }
    
}
