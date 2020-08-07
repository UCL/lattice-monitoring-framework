// PipeProcess.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

// Borrowed from VLSP

package mon.lattice.appl.demo.iot.process;

import java.io.IOException;
import java.lang.reflect.Field;


/**
 * A process that runs down the end of a pipe.
 * Waits for the process to end to grab all the output.
 */
public class PipeProcess extends ProcessWrapper {
    StringBuilder builder;
    String pipeData;
    boolean error = false;

    /**
     * A ProcessWrapper wraps a Process with a name.
     */
    public PipeProcess(Process proc){
        super(proc, "pipe-" + proc.hashCode());
        builder = new StringBuilder();
    }

    /**
     * Print out some input.
     */
    @Override
    public void print(StreamIdent ident, String label, String line) {
        // could check if label is 'stderr' or 'stdout'
        // and do different things
        if (ident.equals(StreamIdent.Stderr)) {
            //System.err.println("PipeProcess: stderr " + line);
        } else {
            // it's stdout
            //System.err.println("PipeProcess: stdout " + line);
            builder.append(line);
            builder.append("\n");
        }
    }

    /**
     * It's EOF
     * Converts builder to Strng
     * Or set null, if error
     */
    @Override
    public void eof(StreamIdent ident) {
        //System.err.println("PipeProcess: EOF");
        if (error)
            pipeData = null;
        else
            pipeData = builder.toString();
    }

    /**
     * There has been an IO error
     */
    @Override
    public void ioerror(StreamIdent ident, IOException ioe) {
        System.err.println("PipeProcess: " + ident + " Got IOException " + ioe);
        error = true;
        pipeData = null;
        //stop();
    }

    /**
     * Get process ID
     */
    public int getPID(){
        try {
            Process process = getProcess();
            Field field = process.getClass().getDeclaredField("pid");
            field.setAccessible(true);
            return field.getInt(process);
        }   catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get the data
     * Returns null if an error has occured.
     */
    public String getData(){
        if (error) {
            return null;
        } else {
            if (pipeData == null)
                return null;
            else
                return pipeData;
        }
    }

    /**
     * Stop the process wrapper.
     */
    @Override
    public void stop(){
        try {
            super.stop();

            iThread.join();
            eThread.join();
        } catch (Exception e) {
        }
    }

    @Override
    protected void terminate(){
        //System.err.println("PipeProcess: terminate " + getPID());
    }
}
