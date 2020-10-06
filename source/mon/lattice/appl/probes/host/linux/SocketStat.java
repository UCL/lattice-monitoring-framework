// ProcStat.java
// Author: Francesco Tusa
// Date: October 2020

package mon.lattice.appl.probes.host.linux;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * A class used to get UDP Socket info on a Linux system.
 * 
 * It uses /proc/net/udp6 to read the underyling data.
 */
public class SocketStat {
    // Socket port
    int socketPort;
    String socketPortAsHex;

    // The //proc/net/udp6 file
    // TODO: note that this should check UDP too
    File socketStat;

    // a map of the last snapshot from /proc/pid/stat
    //HashMap<String, Long> lasttime = new HashMap<String, Long>();
    
    private SocketStatData socketStatData;
    

    // last timestamp
    long lastSampleProc = System.currentTimeMillis();

    /**
     * Construct a ProcStat object.
     */
    public SocketStat(int port) {
        this.socketPort = port;
        socketPortAsHex = Integer.toHexString(socketPort).toUpperCase();
        this.socketStat = new File("/proc/net/udp6");
        socketStatData = new SocketStatData();
    }


    
    public SocketStatData getSocketStatData() {
        return socketStatData;
    }



    /**
     * Read some data from /proc/stat.
     * If calculate is true, then calculate the deltas between 
     * this read and the last read.
     */
    public boolean read() {
        boolean res = socketInfo();
        return res;
    }

    /**
     * Process data from /proc/net/udp6

    * Looks like:
    * sl  local_address                         remote_address                        st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode ref pointer drops
    * 1368: 00000000000000000000000000000000:006F 00000000000000000000000000000000:0000 07 00000000:00000000 00:00000000 00000000     0        0 15301 2 ffff8f1151bb0000 0
    */
    
    public boolean socketInfo() {
        // time when read
        long now = System.currentTimeMillis();
        
        String results =  readSocketStat(socketStat);

	if (results == null) {
            lastSampleProc = now;
	    return false;

	} else {

            String[] parts = results.trim().split("\\s+");

            String localAddress = parts[1].split(":")[0];
            String remoteAddress = parts[2].split(":")[0];
            int remotePort = Integer.parseInt(parts[2].split(":")[1], 16);
            
            long txQueue = Long.parseLong(parts[4].split(":")[0], 16);
            long rxQueue = Long.parseLong(parts[4].split(":")[1], 16);
            
            long drops = Long.parseLong(parts[12]);
            
            
            socketStatData.localAddress = localAddress;
            socketStatData.remoteAddress = remoteAddress;
            socketStatData.remotePort = remotePort;
            
            
            socketStatData.txQueue = txQueue;
            socketStatData.rxQueue = rxQueue;
            
            socketStatData.drops = drops;

            // save timestamp
            lastSampleProc = now;
            
            return true;
        }
    }



    private String readSocketStat(File socketStat) {
	String line;

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(socketStat));
            
	    while ((line = reader.readLine()) != null) {
                if (line.contains(":" + socketPortAsHex + " "))
                    break;
            }

            reader.close();
            return line;
        } catch (Exception e) {
	    // something went wrong
            System.err.println("Exception");
	    return null;
	}

    }
    
    
    
    class SocketStatData {
        String localAddress;
        String remoteAddress;
        int remotePort;
        
        long txQueue;
        long rxQueue;
            
        long drops;
    }
    

}
