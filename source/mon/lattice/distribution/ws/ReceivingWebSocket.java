// ReceivingWebSocket.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation.
 */
public class ReceivingWebSocket extends WebSocketServer {
    // The readQueue holds all the incoming messages.
    // They are taken off by the read() method.
    LinkedBlockingQueue<ByteBuffer> readQueue;
    
    public ReceivingWebSocket( int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        readQueue = new LinkedBlockingQueue();
    }

    public ReceivingWebSocket( InetSocketAddress address ) {
        super( address );
        readQueue = new LinkedBlockingQueue();
    }


    /**
     * Read something from the Socket
     */
    public ByteBuffer read() throws IOException {
        try {
            return readQueue.take();
        } catch (InterruptedException ie) {
            throw new IOException("Read Exception: " + ie.getMessage());
        }            
    }
    
    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        //conn.send("Welcome to the server!"); //This method sends a message to the new client
        //broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        //System.err.println("New connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " handshake: " + handshake );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        //broadcast( conn + " has left the room!" );
        //System.err.println("Closed: " + conn );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        //broadcast( message );
        //System.err.println("Message: " + conn + " --> " + message );

        // convert string to ByteBuffer
        ByteBuffer bb = ByteBuffer.wrap(message.getBytes());
        readQueue.add(bb);
    }
    
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        //broadcast( message.array() );
        //System.err.println("Message: " + conn + " --> " + message );
        //byte[] arr = message.array();
        //System.err.println(arr.length);
        //for (int b=0; b < arr.length; b++) {
        //    System.err.print(String.format("%02d ",arr[b]&0xFF));
        //}
        //System.err.println();
        readQueue.add(message);
    }


    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    public static void main( String[] args ) throws InterruptedException , IOException {
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        ReceivingWebSocket s = new ReceivingWebSocket( port );
        s.start();

        System.err.println( "ReceivingWebSocket started on port: " + s );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }
    }
}
