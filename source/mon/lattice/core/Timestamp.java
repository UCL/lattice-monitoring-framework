// Timestamp.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Jan 2009

package mon.lattice.core;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.TimeZone;
import java.util.Date;

/**
 * A timestamp.
 */
public class Timestamp implements Serializable {
    /**
     * the value of a timestamp.
     */
    long timestamp = 0;


    /*
     * A format for a time
     */
    protected final static SimpleDateFormat tsFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /*
     * A format for milliseconds.  3 obligatory digits.
     */
    protected final static NumberFormat millisformat = new DecimalFormat("000");

    /*
     * A format for the day
     */
    protected final static SimpleDateFormat dayFormatter = new SimpleDateFormat("HH:mm:ss");

    static {
        // ignore DST
        dayFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Construct a Timestamp.
     */
    public Timestamp(long t) {
	timestamp = t;
    }

    /**
     * Get the value of a timestamp.
     */
    public long value() {
	return timestamp;
    }

    /**
     * Do this_timestamp - other_timestamp.
     */
    public Timestamp minus(Timestamp t) {
	if (t != null) {
	    return new Timestamp(timestamp - t.value());
	} else {
	    return new Timestamp(0);
	}
    }

    /**
     * Do this_timestamp + other_timestamp.
     */
    public Timestamp plus(Timestamp t) {
	if (t != null) {
	    return new Timestamp(timestamp + t.value());
	} else {
	    return new Timestamp(0);
	}
    }

    /**
     * To String.
     * Returns long value as String
     */
    public String toString() {
	return "" + timestamp;
    }

    /**
     * Print a Timestamp in format: yyyy/MM/dd HH:mm:ss.000
     */
    public static String format(Timestamp t) {
	long milliseconds = t.timestamp;
	long millisOnly = milliseconds  / 1000;

	return tsFormatter.format((new Date(milliseconds))) + "." + millisformat.format(millisOnly);
    }

    
    /**
     * Print a Timestamp in format: yyyy/MM/dd HH:mm:ss.000
     */
    public static String format(long timestamp) {
	long milliseconds = timestamp;
	long millisOnly = milliseconds  / 1000;

	return tsFormatter.format((new Date(milliseconds))) + "." + millisformat.format(millisOnly);
    }


    /**
     * Print a Timestamp in format: HH:mm.ss.000
     */
    public static String elapsed(long timestamp) {
	long milliseconds = timestamp;
	long millisOnly = milliseconds  / 1000;

	return dayFormatter.format((new Date(milliseconds))) + "." + millisformat.format(millisOnly);
    }
}
