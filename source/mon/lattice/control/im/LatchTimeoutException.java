package mon.lattice.control.im;

/**
 *
 * @author uceeftu
 */
public class LatchTimeoutException extends RuntimeException {

    public LatchTimeoutException() {
    }

    LatchTimeoutException(String msg) {
        super(msg);
    }
    
}
