// ProcessListener.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

// Borrowed from VLSP


package mon.lattice.appl.demo.iot.process;


/**
 * Listen for where a Process goes away
 */
public interface ProcessListener {
    /**
     * The Process has gone away and the input stream was closed.
     */
    public void processEnded(Process p, String name);

    /**
     * The Process has gone away and this is the exitValue
     */
    public void processExitValue(Process p, int exitValue, String name);

}
