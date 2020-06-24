/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.distribution.zmq;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQDataForwarder extends Thread {
    ZContext context;
    ZMQ.Socket backend;
    ZMQ.Socket frontend;
    
    String internalURI = "inproc://dataplane";
    
    int localPort;
    
    public ZMQDataForwarder(int localPort) {
        this.localPort = localPort;
        
        context = new ZContext(1);
        backend = context.createSocket(SocketType.XPUB);
        frontend = context.createSocket(SocketType.XSUB);
    }

    public ZContext getContext() {
        return context;
    }
    
    public String getInternalURI() {
        return internalURI;
    }
    
    public boolean startProxy() {
        this.setName("zmq-data-forwarder");
        frontend.setLinger(0);
        backend.setLinger(0);
        frontend.bind("tcp://*:" + localPort);
        backend.bind("tcp://*:" + (localPort + 1));
        backend.bind(internalURI);
        this.start();
        return true;
    }
    
    public boolean stopProxy() {
        frontend.close();
        backend.close();
        context.destroy();
        return true;
    }
    
    @Override
    public void run() {
        ZMQ.proxy(frontend, backend, null);
    }
}
