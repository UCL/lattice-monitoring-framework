// HostInfoProbe.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020

package mon.lattice.appl.demo.iot;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.InetAddress;

import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.data.list.DefaultMList;
import mon.lattice.core.data.list.MList;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.data.table.DefaultTableValue;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableProbeAttribute;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.TableValue;
import mon.lattice.core.data.table.TableException;

import mon.lattice.appl.probes.host.linux.CPUDev;
import mon.lattice.appl.probes.host.linux.MemoryDev;
import mon.lattice.appl.probes.host.linux.NetDev;

import mon.lattice.appl.demo.iot.process.PipeProcess;

/**
 * A probe that gets info on a host
 */
public class HostInfoProbe extends AbstractProbe implements Probe {
    CPUDev cpuDev;

    MemoryDev memDev;

    HashMap<String, NetDev> netDevs;

    TableHeader netStatsHeader;

    String hostname;

    /**
     * Construct a HostInfoProbe
     */
    public HostInfoProbe(String hostname, int datarate) {
        netDevs = new HashMap<String, NetDev>();

        // set hostname
        this.hostname = hostname;


        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("mac")) {
            // Mac OS
            macosSetup();

        } else if (osName.startsWith("linux")) {
            // Linux
            linuxSetup();

        } else {
            throw new Error("HostInfoProbe: not implemented for " + osName + " yet!");
        }


        // set probe name
        setName(hostname+".hostInfo");
        // set data rate
        setDataRate(new EveryNSeconds(datarate));


        // setup the probe attributes
        // The hostname
        addProbeAttribute(new DefaultProbeAttribute(0, "Name", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu-user", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "cpu-sys", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(3, "cpu-idle", ProbeAttributeType.FLOAT, "percent"));

	// Lefteris: I added the CPU Load average from /proc/loadavg on linux
        // Stuart: Moved to 4 - also for MacOS
	addProbeAttribute(new DefaultProbeAttribute(4, "load-average", ProbeAttributeType.FLOAT, "percent"));

        addProbeAttribute(new DefaultProbeAttribute(5, "mem-used", ProbeAttributeType.INTEGER, "Mb"));
        addProbeAttribute(new DefaultProbeAttribute(6, "mem-free", ProbeAttributeType.INTEGER, "Mb"));
        addProbeAttribute(new DefaultProbeAttribute(7, "mem-total", ProbeAttributeType.INTEGER, "Mb"));

        // Add all network interfaces with traffic
        netStatsHeader = new DefaultTableHeader().
            add("if-name", ProbeAttributeType.STRING).
            add("in-packets", ProbeAttributeType.LONG).
            add("in-bytes", ProbeAttributeType.LONG).
            add("out-packets", ProbeAttributeType.LONG).
            add("out-bytes", ProbeAttributeType.LONG);

        addProbeAttribute(new TableProbeAttribute(8, "net-stats", netStatsHeader));

    }

    /**
     * Collect a measurement.
     */
    @Override
    public ProbeMeasurement collect() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.startsWith("mac")) {
                // Mac OS
                return macos();
            } else if (osName.startsWith("linux")) {
                // Linux
                return linux();
            } else {
                throw new Error("HostInfoProbe: not implemented for " + osName + " yet!");
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Setup for macos
     */
    protected void macosSetup() {
    }

    /**
     * Get data for MacOS
     */
    protected ProducerMeasurement macos() {
        findNetworkInterfaces();
        
        try {
            // create a list for Probe Values
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();


            // add the name of the controller
            list.add(new DefaultProbeValue(0, hostname));


            // get some data from "top"
            PipeProcess top = startMacOSProcess("/usr/bin/env top -l 1 -n 0 -F");

            String topData = getDataFromPipeProcess(top);

            processMacosTopData(topData, list);
            
            // get some data from "netstat"
            PipeProcess netstat = startMacOSProcess("/usr/bin/env netstat -b -i -n");
                
            String netData = getDataFromPipeProcess(netstat);


            processMacosNetstatData(netData, list);


            // Create the Measurement
            ProducerMeasurement m = new ProducerMeasurement(this, list, "HostInfo");


            //System.err.println("m = " + m);

            return m;
        } catch (IOException e) {
            // a failure somewhere
            return null;
        } catch (TypeException te) {
            return null;

        }
    }

    /**
     * Setup for linux
     */
    protected void linuxSetup() {

        cpuDev = new CPUDev();

        // base reading
        cpuDev.read(false);

        memDev = new MemoryDev();
            
        // networks done on the fly 
    }

    /**
     * Get data for linux
     */
    protected ProducerMeasurement linux() {
        findNetworkInterfaces();
        
        try {
            // create a list for Probe Values
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();


            // add the name of the controller
            list.add(new DefaultProbeValue(0, hostname));

            /* CPU */

            // delta reading
            cpuDev.read(true);

            ArrayList<String> keyList = new ArrayList<String>(cpuDev.dataKeys());

            //System.err.println("cpuDev info => " + keyList);

            int cpuNo = keyList.size() / 4;

            float user = 0;
            float sys  = 0;
            float idle = 0;

            for (int cpu = 0; cpu < cpuNo; cpu++) {
                user += cpuDev.getDeltaValue("cpu" + cpu + "-user") +  cpuDev.getDeltaValue("cpu" + cpu + "-nice");
                sys += cpuDev.getDeltaValue("cpu" + cpu + "-system");
                idle += cpuDev.getDeltaValue("cpu" + cpu + "-idle");

                //java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

                /* System.err.println("CPU: " + df.format(cpu) + 
                   " user = " + df.format(user) +
                   " system = " + df.format(sys) +
                   " idle = " + df.format(idle)); */
            }


            list.add(new DefaultProbeValue(1, user/cpuNo));
            list.add(new DefaultProbeValue(2, sys/cpuNo));
            list.add(new DefaultProbeValue(3, idle/cpuNo));

            // Lefteris: I added the CPU Load average from /proc/loadavg
            list.add(new DefaultProbeValue(4, getLoadAverageLinux() ));


            /*  MEMORY */

            memDev.read();

            int memTotal = memDev.getCurrentValue("MemTotal");
            int memFree = memDev.getCurrentValue("MemFree");
            int cached = memDev.getCurrentValue("Cached");
            int buffers = memDev.getCurrentValue("Buffers");

            int used = memTotal - memFree;
            int reallyUsed = used - (cached + buffers);

            /* System.err.println("memoryInfo => " +
               " total = " + memTotal +
               " free = " + memFree +
               " used = " + used +
               " reallyUsed = " + reallyUsed); */

            // convert to Mbs
            list.add(new DefaultProbeValue(5, reallyUsed/1024));
            list.add(new DefaultProbeValue(6, memFree/1024));
            list.add(new DefaultProbeValue(7, memTotal/1024));


            /* NET */

            // now allocate a table
            Table statsTable = new DefaultTable();
            statsTable.defineTable(netStatsHeader);



            // skip through all the network interfaces
            for (Map.Entry<String, NetDev> entryVal : netDevs.entrySet()) {
                String name = entryVal.getKey();
                NetDev netDev = entryVal.getValue();
                
                // read the data
                if (netDev.read(true)) {
                    // got a result

                    // now collect up the results	
                    long in_bytes = netDev.getCurrentValue("in_bytes");
                    long in_packets = netDev.getCurrentValue("in_packets");
                    //int in_errors = netDev.getDeltaValue("in_errors");
                    //int in_dropped = netDev.getDeltaValue("in_dropped");
                    long out_bytes = netDev.getCurrentValue("out_bytes");
                    long out_packets = netDev.getCurrentValue("out_packets");
                    //int out_errors = netDev.getDeltaValue("out_errors");
                    //int out_dropped = netDev.getDeltaValue("out_dropped");


                    /* System.err.println("netInfo => " +
                       " inBytes = " + in_bytes +
                       " inPackets = " + in_packets +
                       " outBytes = " + out_bytes +
                       " outPackets = " + out_packets); */

                    // add data to ProbeValue list
                    // create a row for data
                    TableRow netIFRow = new DefaultTableRow();


                    // add name of NetIf to row
                    netIFRow.add(new DefaultTableValue(name));

                    // we found some values
                    netIFRow.add(new DefaultTableValue(in_packets));
                    netIFRow.add(new DefaultTableValue(in_bytes));
                    netIFRow.add(new DefaultTableValue(out_packets));
                    netIFRow.add(new DefaultTableValue(out_bytes));

                    // add this row to the table
                    statsTable.addRow(netIFRow);

                } else {
                    // create a row for data
                    TableRow netIFRow = new DefaultTableRow();


                    // add name of NetIf to row
                    netIFRow.add(new DefaultTableValue(name));

                    // we found some values
                    netIFRow.add(new DefaultTableValue(0L));
                    netIFRow.add(new DefaultTableValue(0L));
                    netIFRow.add(new DefaultTableValue(0L));
                    netIFRow.add(new DefaultTableValue(0L));

                    // add this row to the table
                    statsTable.addRow(netIFRow);
                }

            }

            // add data to ProbeValue list
            list.add(new DefaultProbeValue(8, statsTable));


            
            // Create the Measurement
            ProducerMeasurement m = new ProducerMeasurement(this, list, "HostInfo");

            //System.err.println("m = " + m);

            return m;
        } catch (TypeException te) {
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            // a failure somewhere
            return null;
        }


    }


    /**
     * Find all the network interfaces on the host.
     */
    protected void findNetworkInterfaces() {
        // skip through all Network Interfaces - ignore loopback and down
        // update the netDevs map with interface name -> NetDev object

        NetDev netDev;
        
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(nics)) {
                if (ni.isLoopback() || ! ni.isUp()) {
                    continue;
                } else {
                    String niName = ni.getName();
                    netDev = new NetDev(niName);

                    // read data, but calculate nothing
                    netDev.read(false);


                    // now add to map
                    netDevs.put(niName, netDev);
                }
            }
        } catch (SocketException se) {
        }


        /*
        for (java.util.Map.Entry<String, NetDev> entry : netDevs.entrySet()) {
            System.err.println("NetworkInterface: " + entry.getKey());
        }
        */
    }

    private float getLoadAverageLinux() {
        // Lefteris: I added a function that looks up /proc/loadavg
        // this should go to CPUDev

        // The /proc/loadavg file
        final File loadstat = new File("/proc/loadavg");
        
	String line;
	float result=0.0f;

	try {
            BufferedReader reader = new BufferedReader(new FileReader(loadstat));

            // find all lines starting with cpu
            if ((line = reader.readLine()) != null) {
		String[] parts = line.split(" ");

		reader.close();
		result = toFloat(parts[0]);
		
		if (result>100.0) result=100.0f;
		return result;
	    } else {
		reader.close();
		return result;
	    }
	} catch (IOException ioex) {
            ioex.printStackTrace();
            return result;
	}
    }

    /**
     * Start 'top' down the end of a pipe.
     */
    protected PipeProcess startMacOSProcess(String proc) throws IOException {
        // create a subrocess
        String [] processArgs = proc.split(" ");
        ProcessBuilder child = new ProcessBuilder(processArgs);
        Process process = child.start();

        // get a wrapper on the process
        return new PipeProcess(process);
    }


    /**
     * Get Data from PipeProcess
     */
    protected String getDataFromPipeProcess(PipeProcess pipe) throws IOException {
        Process process = pipe.getProcess();

        // wait for the process to actually end
        try {
            process.waitFor();
        } catch (InterruptedException ie) {
            System.err.println("PipeProcess: process wait for error: " + ie);
        }

        pipe.stop();

        // and collect the output
        String result =  pipe.getData();

        if (result == null) {
            return null;
        } else if (result.length() == 0) {
            throw new IOException("PipeProcess: failed to process data");
        } else {
            //System.err.println("PipeProcess: collected " + result.length());

            return result;
        }
    }


    /**
     * Process the data from top, and updates the ProbeValue list.
     * <p>
     * Expecting: <br/>
     * Processes: 246 total, 4 running, 3 stuck, 239 sleeping, 1832 threads 
     * 2014/03/18 16:48:57
     * Load Avg: 0.85, 0.74, 0.74 
     * CPU usage: 3.86% user, 11.15% sys, 84.97% idle 
     * MemRegions: 187131 total, 7462M resident, 0B private, 2246M shared.
     * PhysMem: 3386M wired, 6399M active, 4318M inactive, 14G used, 2277M free.
     * VM: 480G vsize, 0B framework vsize, 41219330(0) pageins, 6670142(0) pageouts.
     * Networks: packets: 86118150/51G in, 76529464/19G out.
     * Disks: 10163200/243G read, 25746920/589G written.
     */
    protected void processMacosTopData(String raw, ArrayList<ProbeValue> list) {
        try { 

            // split lines
            String[] parts = raw.split("\n");

            String loadavg = parts[2];
            String cpu = parts[3];
            String mem = parts[5];
            String net = parts[7];

            // split loadavg
            String[] loadavgParts = loadavg.split("\\s+");

            float loadavgVal = toFloat(loadavgParts[2]);

            // split cpu
            // CPU usage: 3.82% user, 11.6% sys, 85.10% idle 
            String[] cpuParts = cpu.split("\\s+");

            //System.err.println("cpuParts = " + Arrays.asList(cpuParts));


            float cpuUser = toFloat(cpuParts[2]);
            float cpuSys = toFloat(cpuParts[4]);
            float cpuIdle = toFloat(cpuParts[6]);

            list.add(new DefaultProbeValue(1, cpuUser));
            list.add(new DefaultProbeValue(2, cpuSys));
            list.add(new DefaultProbeValue(3, cpuIdle));

            list.add(new DefaultProbeValue(4, loadavgVal));

            // split mem
            // PhysMem: 3411M wired, 6513M active, 4309M inactive, 14G used, 2143M free.
            // PhysMem: 2480M used (666M wired), 2143M unused.
            // or PhysMem: 16G used (3501M wired), 111M unused.
            String[] memParts = mem.split("\\s+");

            //System.err.println("memParts = " + memParts.length + " " + Arrays.asList(memParts));

            int memTotal = 0;
            int memUsed = 0;
            int memFree = 0;

            if (memParts.length == 11) { // style 1
                int used = toInt(memParts[1]) + toInt(memParts[3]) + toInt(memParts[5]);
                int free = toInt(memParts[9]);
                int total = used + free;

                memUsed = used;
                memFree = free;
                memTotal = total;

            } else if (memParts.length == 7) { // style 2
                int used = toInt(memParts[1]) + toInt(memParts[3]);
                int free = toInt(memParts[5]);
                int total = used + free;

                memUsed = used;
                memFree = free;
                memTotal = total;

            } else {
            }

            list.add(new DefaultProbeValue(5, memUsed));
            list.add(new DefaultProbeValue(6, memFree));
            list.add(new DefaultProbeValue(7, memTotal));


            /*
             * not used currently
            // split net
            // Networks: packets: 86128340/51G in, 76539791/19G out.
            String[] netParts = net.split("\\s+");

            //System.err.println("netParts = " + Arrays.asList(netParts));


            String inParts[] = netParts[2].split("/");
            String outParts[] = netParts[4].split("/");

            int inPackets = toInt(inParts[0]);
            int inVolume = toInt(inParts[1]);

            int outPackets = toInt(outParts[0]);
            int outVolume = toInt(outParts[1]);
            */

        } catch (TypeException te) {
            return;
        }
    }


    // Name  Mtu   Network       Address            Ipkts Ierrs     Ibytes    Opkts Oerrs     Obytes  Coll
    // lo0   16384 <Link#1>                      38160393     0 13512249693 38160393     0 13512249693     0
    // lo0   16384 fe80::1%lo0 fe80:1::1         38160393     - 13512249693 38160393     - 13512249693     -
    // lo0   16384 127           127.0.0.1       38160393     - 13512249693 38160393     - 13512249693     -
    // lo0   16384 ::1/128     ::1               38160393     - 13512249693 38160393     - 13512249693     -
    // gif0* 1280  <Link#2>                             0     0          0        0     0          0     0
    // stf0* 1280  <Link#3>                             0     0          0        0     0          0     0
    // en0   1500  <Link#4>    14:10:9f:ce:34:f9 49615246     0 41909285921 39950842     0 7378437798     0
    // en0   1500  fe80::1610: fe80:4::1610:9fff 49615246     - 41909285921 39950842     - 7378437798     -
    // en0   1500  10.111/17     10.111.112.215  49615246     - 41909285921 39950842     - 7378437798     -
    // p2p0  2304  <Link#5>    06:10:9f:ce:34:f9        0     0          0        0     0          0     0
    /**
     * Process the data from netstat, and updates the ProbeValue list.
     */
    protected void processMacosNetstatData(String raw, ArrayList<ProbeValue> list) {
        try { 
            // now allocate a table
            Table statsTable = new DefaultTable();
            statsTable.defineTable(netStatsHeader);

            // split lines
            String[] parts = raw.split("\n");

            // skip through all lines
            for (String part : parts) {
                String[] words = part.split("\\s+");

                /* TODO: convert data to Table
                   if-name as value 0 
                */

                // found an interface we are interested in
                if (words.length == 11 && netDevs.containsKey(words[0]) && words[10].equals("0")) {

                    // create a row for data
                    TableRow netIFRow = new DefaultTableRow();


                    // add name of NetIf to row
                    netIFRow.add(new DefaultTableValue(words[0]));

                    // we found some values
                    netIFRow.add(new DefaultTableValue(toLong(words[4])));
                    netIFRow.add(new DefaultTableValue(toLong(words[6])));
                    netIFRow.add(new DefaultTableValue(toLong(words[7])));
                    netIFRow.add(new DefaultTableValue(toLong(words[9])));

                    // add this row to the table
                    statsTable.addRow(netIFRow);

                } else {
                    continue;
                }
            }

            // add data to ProbeValue list
            list.add(new DefaultProbeValue(8, statsTable));

        } catch (TypeException te) {
            return;
        } catch (TableException tbe) {
            tbe.printStackTrace();
            return;
        }
    }


    private int toInt(String s) {
        // drop M or G at end
        String numStr = "";
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(s); 
        if (m.find()) {
            numStr = m.group();
        }

        // cvt to Integer
        Scanner sc = new Scanner(numStr);
        // int i = sc.nextInt(); // This doesnt work for some people
        int i = Integer.parseInt(sc.next());

        if (s.endsWith("G")) {
            return i * 1024;
        } else {
            // endsWith("M")
            return i;
        }
    }

    private long toLong(String s) {
        // drop M at end
        String numStr = "";
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(s); 
        if (m.find()) {
            numStr = m.group();
        }

        // cvt to Integer
        Scanner sc = new Scanner(numStr);
        //long l = sc.nextLong();
        long l = Long.parseLong(sc.next());
        
        return l;
    }

    private float toFloat(String s) {
        // drop M at end
        String numStr = "";
        Pattern p = Pattern.compile("[\\d\\.]+");
        Matcher m = p.matcher(s); 
        if (m.find()) {
            numStr = m.group();
        }

        // cvt to Integer
        Scanner sc = new Scanner(numStr);
        //float f = sc.nextFloat();
        float f = Float.parseFloat(sc.next());

        return f;
    }
}
