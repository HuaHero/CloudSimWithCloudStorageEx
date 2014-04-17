package cloudStorage;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * @author huahero
 *
 */
public class ReplicaCloudlet extends Cloudlet{

	/** The node id. */
	protected int nodeId;
	/**The replica Id*/
	protected int replicaId;
	public ReplicaCloudlet(int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		// TODO Auto-generated constructor stub
		this.nodeId = -1;
		this.replicaId = -1;
	}
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(final int nodeId) {
		this.nodeId = nodeId;
	}
	public int getReplicaId() {
		return replicaId;
	}
	public void setReplicaId(final int replicaId) {
		this.replicaId = replicaId;
	}

}
