// SendingWebSocket.java
// Author: Stuart Clayman
// Email: s.clayman@ucl.ac.uk
// Date: May 2020

package mon.lattice.distribution.ws;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.LoggerFactory;

public class SendingWebSocket extends WebSocketClient {

    public SendingWebSocket( URI serverURI ) {
        super( serverURI );
    }

    public SendingWebSocket( URI serverUri , Draft draft ) {
        super( serverUri, draft );
    }

    public SendingWebSocket( URI serverUri, Map<String, String> httpHeaders ) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        //send("Hello, it is me. Mario :)");
        System.err.println( "SendingWebSocket: Opened connection: handshake: " + handshakedata);
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage( String message ) {
        //System.err.println( "received: " + message );
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        LoggerFactory.getLogger(SendingWebSocket.class).info("SendingWebSocket: Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
    }

    @Override
    public void onError( Exception ex ) {
        System.err.println( "SendingWebSocket: Error: " + ex);
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    public static void main( String[] args ) throws URISyntaxException {
        SendingWebSocket c = new SendingWebSocket( new URI( "ws://localhost:8887" ));
        c.connect();

        System.err.println( "SendingWebSocket connected: " + c);
        
    }

}
