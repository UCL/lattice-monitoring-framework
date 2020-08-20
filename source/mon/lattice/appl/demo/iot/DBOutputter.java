// DBOutputter.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: Aug 2020

package mon.lattice.appl.demo.iot;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import cc.clayman.logging.LogOutput;
import cc.clayman.logging.LogInput;


/**
 * A DBOutputter  that will do output for a Logger.
 */
public class DBOutputter implements LogOutput {
    PrintWriter printWriter;

    public DBOutputter(String connection) throws IOException {
        // A quick test
        FileOutputStream outputStream = new FileOutputStream(connection);
        printWriter = new PrintWriter(outputStream);
    }

    
    /**
     * Process a string
     */
    public void process(String s) {
        printWriter.println(s);
        printWriter.flush();
    }

    /**
     * Process an LogInput
     * Unlikely to happen in our case.
     */
    public void process(LogInput logObj) {
        process(logObj.logView());
    }

}

