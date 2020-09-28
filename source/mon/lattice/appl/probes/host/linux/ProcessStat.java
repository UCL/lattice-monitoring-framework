// ProcStat.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2020

package mon.lattice.appl.probes.host.linux;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.LinkedList;

/**
 * A class used to get  process info on a Linux system.
 * It calculates cpu and mem usage for last period
 * and also moving average cpu usage.
 * It uses /proc/[pid]/stat to read the underyling data.
 */
public class ProcessStat {
    // Process ID
    int pid;

    // The /proc/[pid]/stat file
    File pidstat; // something like new File("/proc/" + pid + "/stat");

    // a map of the last snapshot from /proc/pid/stat
    HashMap<String, Long> lasttime = new HashMap<String, Long>();
    // a map of the current delta values from /proc/pid/stat
    HashMap<String, Long> thisdelta = new HashMap<String, Long>();
    // a map of cpu percents
    HashMap<String, Float> cpuUsage = new HashMap<String, Float>();

    // last timestamp
    long lastSampleProc = System.currentTimeMillis();


    // Moving average window size
    int windowSize = 0;

    LinkedList<Float> maList = new LinkedList<Float>();

    /**
     * Construct a ProcStat object.
     */
    public ProcessStat(int pid) {
        this.pid = pid;
        this.pidstat = new File("/proc/" + pid + "/stat");
        windowSize = 5;       
    }

    /**
     * Construct a ProcStat object.
     * Also specify the number of cpu readings to keep for the moving average. 
     */
    public ProcessStat(int pid, int maWindow) {
        this.pid = pid;
        this.pidstat = new File("/proc/" + pid + "/stat");
        windowSize = maWindow;
    }

    /**
     * Get the size of the data map.
     */
    public int getDataSize() {
        return lasttime.size();
    }

    /**
     * Get the list of keys based on data read in.
     */
    public Set<String> dataKeys() {
        return lasttime.keySet();
    }

    /**
     * Get current value for a particular element of data.
     */
    public Long getCurrentValue(String key) {
        return lasttime.get(key);
    }

    /**
     * Get the size of the delta map.
     */
    public int getDeltaSize() {
        return thisdelta.size();
    }

    /**
     * Get the list of keys in the delta map.
     */
    public Set<String> deltaKeys() {
        return thisdelta.keySet();
    }


    /**
     * Get delta value, the difference between the current values
     * and the previous value, for a particular element of data.
     */
    public Long getDeltaValue(String key) {
        return thisdelta.get(key);
    }

    /**
     * Get the size of the cpuUsage map.
     */
    public int getCpuUsageSize() {
        return cpuUsage.size();
    }

    /**
     * Get the list of keys in the cpuUsage map.
     */
    public Set<String> cpuUsageKeys() {
        return cpuUsage.keySet();
    }


    /**
     * Get cpuUsage value for a particular element of data.
     */
    public Float getCpuUsageValue(String key) {
        return cpuUsage.get(key);
    }


    /**
     * Read some data from /proc/stat.
     * If calculate is true, then calculate the deltas between 
     * this read and the last read.
     */
    public boolean read(boolean calculate) {
        //processProcStat(calculate);

        boolean res = processPid(calculate);

        return res;
    }

    /**
     * Process data from /proc/[pid]/stat
     *
     * Looks like:
     * 11828 (java) S 11827 11826 9840 0 -1 1077944576 861839 2699 0 0 155627 25293 0 1 20 0 35 0 5868594215 7617949696 219886 18446744073709551615 1 1 0 0 0 0 0 1 16800974 18446744073709551615 0 0 17 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     *
     * Details here:  https://man7.org/linux/man-pages/man5/proc.5.html
     */
    public boolean processPid(boolean calculate) {
        // time when read
        long now = System.currentTimeMillis();

	List<String> results = readPidStat(pidstat);

	if (results == null) {
            lastSampleProc = now;
	    return false;

	} else {
            // get "the" line
            String line = results.get(0);

            String[] parts = line.split(" ");

            //System.err.println("parts: " + parts.length + " " + Arrays.asList(parts));

            // 14 and 15 are cpu related
            // - 14: Amount of time that this process has been scheduled in user mode, measured in clock ticks
            // - 15: Amount of time that this process has been scheduled in kernel mode, measured in clock ticks 
            // 23 and 24 are mem related
            // - 23: Virtual memory size in bytes
            // - 24: Resident Set Size: number of pages the process has in real memory.
            long userN = Long.parseUnsignedLong(parts[13]);
            long systemN = Long.parseUnsignedLong(parts[14]);

            long total = userN + systemN;

            long vmRSSN = Long.parseUnsignedLong(parts[23]);

            long vmRSSBytes = vmRSSN * 4096;   // 4K pages

            /*
            System.err.println("data => " + pid +
                               " calculate = " + calculate +
                               " user = " + userN +
                               " system = " + systemN +
                               " total = " + total);
            */
            
            // determine millis since last read
            int millis = (int)(now - lastSampleProc);


                // determine if we need to calculate the deltas
            // from the raw data
            if (calculate) {  // as a %age
                long userDiff = userN - (Long)lasttime.get("proc-user");
                long systemDiff = systemN - (Long)lasttime.get("proc-system");

                long totalDiff = total - (Long) lasttime.get("proc-total");

                Float percent = ((float)totalDiff) / ((float)millis/1000);

                // add a value to the moving average list
                if (maList.size() == windowSize) {
                    // drop an element
                    maList.remove(0);
                }

                if (! percent.isNaN()) {
                    maList.add(percent);
                }

                float maCPU = average(maList);
                

                /*
                System.err.println("ps => " + pid + ":" +
                                   //" millis = " + millis +
                                   " total = " + String.format("%3d", totalDiff) +
                                   " user = " + String.format("%3d", userDiff) +
                                   " system = " + String.format("%3d", systemDiff) +
                                   " percent = " + String.format("%6.2f", percent) +
                                   " mov avg = " + String.format("%6.2f", maCPU) +
                                   " mem = " + (float)vmRSSBytes / (1024 * 1024) + " Mb"
                                   );                
                */
                
                // cpu
                cpuUsage.put("cpu-percent", percent);
                cpuUsage.put("cpu-average", maCPU);

                // deltas
                thisdelta.put("proc-user", userDiff);
                thisdelta.put("proc-system", systemDiff);


                // memory
                long rssDiff = vmRSSBytes - (Long)lasttime.get("proc-rss");
                thisdelta.put("proc-rss", rssDiff);

            }

            lasttime.put("proc-user", userN);
            lasttime.put("proc-system", systemN);            
            lasttime.put("proc-total", total);

            // memory
            lasttime.put("proc-rss", vmRSSBytes);

            // save timestamp
            lastSampleProc = now;
            
            return true;
        }
    }


    /**
     * Read from /proc/[pid]/stat
     *
     * Looks like:
     * 11828 (java) S 11827 11826 9840 0 -1 1077944576 861839 2699 0 0 155627 25293 0 1 20 0 35 0 5868594215 7617949696 219886 18446744073709551615 1 1 0 0 0 0 0 1 16800974 18446744073709551615 0 0 17 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     */
    private List<String> readPidStat(File pidstat) {
	LinkedList<String> lines = new LinkedList<String>();
	String line;

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(pidstat));

	    // find all lines starting with cpu
	    while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            reader.close();

            return lines;
        } catch (Exception e) {
	    // something went wrong
	    return null;
	}

    }
    

    /**
     * Average a list
     */
    private float average(LinkedList<Float> list) {
        int n = list.size();
        float sum = 0.0f;

        for (float v : list) {
            sum += v;
        }

        return sum / n;
    }
}
