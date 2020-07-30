/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import java.util.HashSet;
import java.util.Set;
import mon.lattice.control.im.ControlInformation;
import mon.lattice.control.im.ControlInformationInteracter;
import mon.lattice.core.EntityType;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AnnounceMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQRouter implements Runnable, ControlInformationInteracter {
    private static Logger LOGGER = LoggerFactory.getLogger(ZMQRouter.class);
    
    ZMQ.Context context;
    ZMQ.Socket frontend;
    ZMQ.Socket backend;
    
    int backendPort;
    
    Set<String> workers = new HashSet<>();
    
    Thread router;
    
    ControlInformation controlInformation;
    

    public ZMQRouter(int port) {
        backendPort = port;
        context = ZMQ.context(1);
        frontend = context.socket(SocketType.ROUTER);
        backend = context.socket(SocketType.ROUTER);
    }
    
    public void bind() {
        frontend.setLinger(0);
        frontend.setHWM(0);
        frontend.setRcvHWM(0);
        backend.setLinger(0);
        backend.setHWM(0);
        backend.setRcvHWM(0);
        frontend.bind("inproc://frontend");
        backend.bind("tcp://*:" + backendPort);
        router = new Thread(this, "zmq-router");
        router.start();
    }
    
    public ZMQ.Context getContext() {
        return this.context;
    }
    
    
    public void disconnect() {
        frontend.close();
        backend.close();
        context.term();
    }

    @Override
    public void run() {
        ZMQ.Poller items = context.poller(2);
        items.register(backend, ZMQ.Poller.POLLIN);
        items.register(frontend, ZMQ.Poller.POLLIN);
        
        while (!Thread.currentThread().isInterrupted()) {
            if (items.poll() < 0)
                break;
            
            if (items.pollin(0)) {
                // we have message on the backend, it is a worker identity
                // identity is added to the workers set
                String workerIdentity = backend.recvStr(); 
                workers.add(workerIdentity);
                
                backend.recvStr(); // empty frame
                
                // worker sends the client identity to which the reply message
                // has to be sent to (or READY)
                String clientToReplyAddress = backend.recvStr();
                
                if (!clientToReplyAddress.equals("READY")) { // it is an actual identity
                    backend.recvStr(); // empty frame
                    
                    // reply message from the worker
                    byte[] reply = backend.recv();
                    
                    // sending the reply to the specified client identity
                    frontend.sendMore(clientToReplyAddress);
                    frontend.sendMore("");
                    
                    // sending worker identity
                    frontend.sendMore(workerIdentity);
                    frontend.sendMore("");
                    
                    frontend.send(reply);
                }
                
                /* we use this message (READY) to allow synchronization with the management 
                   and info plane threads. We lock the management thread in the
                   ControlInformationManager until we receive READY from 
                   a new deployed entity (with ID workerIdentity).
                */
                
                else {
                    String entityType =  backend.recvStr();
                    AnnounceMessage m = new AnnounceMessage(ID.fromString(workerIdentity), EntityType.valueOf(entityType));
                    controlInformation.notifyAnnounceEvent(m);
                }
            }
            
            if (items.pollin(1)) {
                String clientAddress = frontend.recvStr(); // clientAddress
                frontend.recvStr(); // empty delimiter frame
                
                String workerAddress = frontend.recvStr();
                
                frontend.recvStr(); // empty frame
                byte[] message = frontend.recv();
                
                if (workers.contains(workerAddress)) { // if we can find the worker
                    // first we specify the worker 
                    backend.sendMore(workerAddress);
                    backend.sendMore("");
                    
                    // we send information about the client sending the message
                    backend.sendMore(clientAddress);
                    
                    // we send the actual message
                    backend.sendMore("");
                    backend.send(message);
                }
            }
        }
        
    }

    @Override
    public ControlInformation getControlInformation() {
        return controlInformation;
    }

    @Override
    public void setControlInformation(ControlInformation ci) {
        controlInformation = ci;
    }
    
    
            
    
}
