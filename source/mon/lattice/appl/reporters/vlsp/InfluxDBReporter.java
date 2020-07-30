package mon.lattice.appl.reporters.vlsp;

import java.io.IOException;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.Timestamp;
import java.util.List;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An InfluxDBReporter stores metrics into InfluxDB.
 */
public final class InfluxDBReporter extends AbstractControllableReporter {
    /**
     * In this InfluxDBReporter, report() logs and formats the Measurement (from VLSP) to an InfluxDB Database.
     */
    
    String serverAddress;
    String serverPort;
    String influxDBURI;
    String database;
    
    Resty resty = new Resty();

    private static Logger LOGGER = LoggerFactory.getLogger(InfluxDBReporter.class);
    
    
    public InfluxDBReporter(String reporterName, String address, String port, String database) {
        super(reporterName); 
        this.serverAddress = address;
        this.serverPort = port;
        this.database= database;
        this.influxDBURI = "http://" + serverAddress + ":" + serverPort + "/write?db=" + database + "&precision=ms";
    }
    
    
    @Override
    public void report(Measurement m) {
        List<ProbeValue> values = m.getValues();
        
        // we get the first Probe value containing the whole table
        ProbeValue tableValue = values.get(0);
        
        // we get the whole table and the table header
        Table table = (Table)tableValue.getValue();
        TableHeader columnDefinitions = table.getColumnDefinitions();

        int rowsNumber = table.getRowCount();
        int columnsNumber = table.getColumnCount();
        TableRow row;

        String cpuMetric=null;
        String memMetric=null;

        Timestamp timestamp = m.getTimestamp();
        String sliceID = m.getGroupID().toString();
       
        for (int i=0; i < rowsNumber; i++) {
            row = table.getRow(i);
            
            String partID = row.get(0).getValue().toString();
            String hostName = row.get(1).getValue().toString();
            String cpuValue = row.get(2).getValue().toString();
            String memValue = row.get(4).getValue().toString();

            cpuMetric = "cpuLoad," + "sliceid=" + sliceID + ",partid=" + partID + ",hostname=" + hostName + " value=" + cpuValue + " " + timestamp;
            memMetric = "usedMemory," + "sliceid=" + sliceID + ",partid=" + partID + ",hostname=" + hostName + " value=" + memValue + " " + timestamp;
            
            LOGGER.debug(cpuMetric);
            LOGGER.debug(memMetric);

            try {
                resty.json(influxDBURI, form(cpuMetric.toString()));
                resty.json(influxDBURI, form(memMetric.toString()));
            } catch (IOException e) {
                LOGGER.error("Error while writing measurement to the DB: " + e.getMessage());
                for (int j=0; i< e.getStackTrace().length; j++)
                    LOGGER.error(e.getStackTrace()[j].toString());
              }

        }
        
    }
}
