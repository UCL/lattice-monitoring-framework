package mon.lattice.distribution;

import java.util.List;

import mon.lattice.core.ID;
import mon.lattice.core.ProbeValue;

public class ConsumerMeasurementWithMetaDataAndProbeName extends ConsumerMeasurementWithMetaData implements WithNames {
	public String probeName;

	public ConsumerMeasurementWithMetaDataAndProbeName(long seqNo, ID pid,
			String theType, long ts, long delta, ID serviceID, ID groupID,
			List<ProbeValue> attrs, String probeName) {
		super(seqNo, pid, theType, ts, delta, serviceID, groupID, attrs);
		this.probeName = probeName;
	}

	public String getProbeName() {
		return probeName;
	}

    /**
     * To String
     */
    public String toString() {
	return ("seq: " + getSequenceNo() + " probename: " + getProbeName() + " probeid: " + getProbeID() + " serviceid: " + getServiceID() + " groupid: " + getGroupID() + " timestamp: " +  getTimestamp() + " delta: " + getDeltaTime() + " type: " + getType() + " attributes: " + getValues());
    }
	
}

