package mon.lattice.im;

import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;

/**
 * @author Francesco Tusa
 * An AbstractIMNodeWithAnnounce extends AbstractIMNode with the
 * functionalities required to notify an AnnounceEventListener 
 * when an Announce message is received
 */
public abstract class AbstractIMNodeWithAnnounce extends AbstractIMNode implements AnnounceHandler {
    
    protected AnnounceEventListener listener;
    
    @Override
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    @Override
    public void sendMessageToListener(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
}
