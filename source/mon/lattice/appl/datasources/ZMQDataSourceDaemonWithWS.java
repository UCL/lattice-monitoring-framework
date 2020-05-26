/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.datasources;

import java.io.IOException;
import java.net.UnknownHostException;
import mon.lattice.distribution.ws.WSDataPlaneProducerWithNames;


public class ZMQDataSourceDaemonWithWS extends ZMQDataSourceDaemon {

    public ZMQDataSourceDaemonWithWS(String myID, String myDSName, String dataConsumerName, int dataConsumerPort, String infoPlaneRootName, int infoPlaneRootPort, String controlHostAddress, int controlHostPort) throws UnknownHostException {
        super(myID, myDSName, dataConsumerName, dataConsumerPort, infoPlaneRootName, infoPlaneRootPort, controlHostAddress, controlHostPort);
    }

    @Override
    public void init() throws IOException {
        super.init();
        dataSource.setDataPlane(new WSDataPlaneProducerWithNames(dataConsumerPair)); 
    }
    
    
    public static void main(String[] args) {
        ZMQDataSourceDaemon.main(args);
    }
    
}
