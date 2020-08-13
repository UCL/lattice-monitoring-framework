// TcpdumpWrapper.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: July 2020


package mon.lattice.appl.demo.iot.process;

import java.io.StringReader;
import java.io.IOException;
import java.lang.reflect.Field;
import org.slf4j.LoggerFactory;


/**
 * A process that runs tcpdum down the end of a pipe.
 * It grabs data as it comes up from the process.
 *
 * Runs: sudo tcpdump -ttt -l -n -i eth0 port 80
 *
 * Needs to run in a 'sudo' context to get tcpdump to work
 */
public class TcpdumpWrapper extends ProcessWrapper {
    Tcpdump tcpdump;

    String portSpec = "";

    // Count the traffic
    long totalInBytes = 0;
    long totalOutBytes = 0;
    long totalInPackets = 0;
    long totalOutPackets = 0;

    // debug
    static boolean debug = false;

    /**
     * This ProcessWrapper wraps a Tcpdump Process.
     */
    public TcpdumpWrapper(Tcpdump tcp) {
        super(tcp.getProcess(),  "tcpdump-" + tcp.getProcess().hashCode());

        // keep a handle on the Tcpdump process
        tcpdump = tcp;

        // create a port spec to match against
        // e.g.  ".8080"
        portSpec = "." + tcpdump.getPort();
    }


    /**
     * Get traffic info
     * Returns an array of [Total In (bytes), Total Out  (bytes), Total In (packets), Total Out  (packets)]
     */
    public long[] getTraffic() {
        return new long[]{totalInBytes, totalOutBytes, totalInPackets, totalOutPackets};
    }


    /**
     * Print just collects data.
     */
    @Override
    public void print(StreamIdent ident, String label, String line) {
        // could check if label is 'stderr' or 'stdout'
        // and do different things
        if (ident.equals(StreamIdent.Stderr)) {
             LoggerFactory.getLogger(TcpdumpWrapper.class).debug("TcpdumpWrapper: stderr " + line);
        } else {
            // it's stdout
             LoggerFactory.getLogger(TcpdumpWrapper.class).debug("TcpdumpWrapper: stdout " + line);

            // work out what to do
            tcpdumpLine(line);
        }
    }


    /**
     * Process a line of tcpdump data.
     * Updates totalIn and totalOut.
     * 4   00:00:00.000014 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 0
     * 5   00:00:02.132564 IP 127.0.0.1.60304 > 127.0.0.1.6060: tcp 495
     * 6   00:00:00.000036 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 0
     * 7   00:00:00.128476 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 243
     * 8   00:00:00.000051 IP 127.0.0.1.60304 > 127.0.0.1.6060: tcp 0
     * 9   00:00:00.074251 IP 127.0.0.1.60304 > 127.0.0.1.6060: tcp 497
     * 10   00:00:00.000063 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 0
     * 11   00:00:00.484844 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 9000
     * 12   00:00:00.000056 IP 127.0.0.1.6060 > 127.0.0.1.60304: tcp 738
     * 13   00:00:00.000039 IP 127.0.0.1.60304 > 127.0.0.1.6060: tcp 0
     * 14   00:00:00.000021 IP 127.0.0.1.60304 > 127.0.0.1.6060: tcp 0
     *
     * but might get IPv6 addresses
     *
     * 00:00:00.000000 IP6 ::1.53493 > ::1.80: tcp 0
     */
    protected String tcpdumpLine(String line) {
        try {
            final String format_ipv4 = " %d:%d:%d.%d IP %s > %s: tcp %d";
            final String format_ipv6 = " %d:%d:%d.%d IP6 ::%s > ::%s: tcp %d";

            Object[] vals;

            if (line.contains("IP6")) {
                vals = new FormatReader(new StringReader(line)).scanf(format_ipv6);
            } else {
                vals = new FormatReader(new StringReader(line)).scanf(format_ipv4);
            }
            
            //System.err.println(line);
            String src =  (String)vals[4];
            String dst =  (String)vals[5];
            long length = (long)vals[6];
            
            if (src.endsWith(portSpec)) {
                // sending out

                totalOutBytes += length;
                totalOutPackets += 1;
                
                LoggerFactory.getLogger(TcpdumpWrapper.class).debug("OUT: sender = " + src + " receiver = " + dst + " length = " + length + " totalIn = " + totalInBytes + " totalOut = " + totalOutBytes);
            } else {
                // dst endsWith portSpec
                // incoming

                totalInBytes += length;
                totalInPackets += 1;
                
                LoggerFactory.getLogger(TcpdumpWrapper.class).debug("IN: sender = " + src + " receiver = " + dst + " length = " + length + " totalIn = " + totalInBytes + " totalOut = " + totalOutBytes);
            }

            

            return line;
        } catch (Exception e) {
            LoggerFactory.getLogger(TcpdumpWrapper.class).error(e.getMessage());
            LoggerFactory.getLogger(TcpdumpWrapper.class).error(line);
            for (StackTraceElement el: e.getStackTrace())
                LoggerFactory.getLogger(TcpdumpWrapper.class).error(el.toString());
            return "";
        }
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
     * Test running tcpdump down a pipe
     * Takes an interface name and a port number
     */
    public static void main(String [] args) {
        if (args.length == 2) {
            String interF = args[0];

	    java.util.Scanner sc = new java.util.Scanner(args[1]);
	    int port = sc.nextInt();

            try {
                Tcpdump tcpdump = new TcpdumpWrapper.Tcpdump(interF, port);

                TcpdumpWrapper tcpdumpListener = new TcpdumpWrapper(tcpdump);
                tcpdumpListener.debug = true;
                
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        } else {
	    System.err.println("usage: TcpdumpWrapper interface port");
	    System.exit(1);
        }
    }

    /**
     *  The main tcpdump command
     */
    private static String tcpdumpCmd = "sudo tcpdump -ttttt -l -nn -q";

    /**
     * Build a Process from some args
     */
    static public class Tcpdump  {
        //-i lo0 port ";

        String proc = null;

        Process process = null;

        // host specifier for tcpdump
        String host = "*";

        // interface specifier for tcpdump
        String ifName = "";

        // port specifier for tcpdump
        int port = 0;

        public Tcpdump(String interF, int port) throws IOException {
            this.port = port;
            this.ifName = interF;
            proc = fullCommand(interF, port);
            startProcess();
        }
        
        public Tcpdump(String interF, String host, int port) throws IOException {
            this.host = host;
            this.ifName = interF;
            this.port = port;
            proc = fullCommand(interF, host, port);
            startProcess();
        }
        
        /**
         * Start 'tcpdump' down the end of a pipe.
         */
        protected Process startProcess() throws IOException {
            // create a subrocess
            String [] processArgs = proc.split(" ");
            ProcessBuilder child = new ProcessBuilder(processArgs);
            process = child.start();

            return process;
        }

        /**
         * Get the Process
         */
        public Process getProcess() {
            return process;
        }

        /**
         * Get the Host
         * Returns "*" if nothing particular specified.
         */
        public String getHost() {
            return host;
        }

        /**
         * Get the interface
         */
        public String getInterface() {
            return ifName;
        }

        /**
         * Get the port
         */
        public int getPort() {
            return port;
        }

        /**
         * Convert the partial tcpdumpCmd into a full command string
         * with passed in args.
         */
        protected String fullCommand(String interF, int port) {
            String full = String.format(tcpdumpCmd + " -i %s port %d", interF, port);

            return full;
        }

        protected String fullCommand(String interF, String host, int port) {
            String full = String.format(tcpdumpCmd + " -i %s host %s and port %d", interF, host, port);

            return full;
        }

        
    }    
}
