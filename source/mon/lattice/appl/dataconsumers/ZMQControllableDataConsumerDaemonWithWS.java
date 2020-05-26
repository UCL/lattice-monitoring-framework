/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

import java.io.IOException;
import java.net.UnknownHostException;
import mon.lattice.distribution.ws.WSDataPlaneConsumerWithNames;


public class ZMQControllableDataConsumerDaemonWithWS extends ZMQControllableDataConsumerDaemon {

    public ZMQControllableDataConsumerDaemonWithWS(String myID, int dataPort, String infoPlaneRootName, int infoPlaneRootPort, String controlAddr, int controlPort) throws UnknownHostException {
        super(myID, dataPort, infoPlaneRootName, infoPlaneRootPort, controlAddr, controlPort);
    }

    @Override
    public void init() throws IOException {
        super.init();
        consumer.setDataPlane(new WSDataPlaneConsumerWithNames(dataPort));
    }
    
    public static void main(String[] args) {
        ZMQControllableDataConsumerDaemon.main(args);
    }
    
    
}
