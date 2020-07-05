package mon.lattice.im;

import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;

/**
 * @author Francesco Tusa
 * An AnnounceHandler is capable of triggering events to a registered Listener
 * when an Announce Message is received
 */
public interface AnnounceHandler {

    void addAnnounceEventListener(AnnounceEventListener l);

    void sendMessageToListener(AbstractAnnounceMessage m);
    
}
