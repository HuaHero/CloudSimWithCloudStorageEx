package cloudStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;

/**
 * ReplicaAllocationPolicySimple is an ReplicaAllocationPolicy that
 * chooses, as the host for a BlockReplica, the host with
 * less Storage(PEs) in use.
 *
 * @author		Hua Hero
 * @since		CloudSim Toolkit 1.0
 */


public class ReplicaAllocationPolicySimple extends ReplicaAllocationPolicy {
	
	/**The replica table*/
	private Map<String,StorageNode> replicaTable;

	//实际要考虑的话，处理器、内存什么的都需要，但这里对存储的分布，所以觉得目前暂时先考虑实现对存储的考虑，仿照VmAllocationPolicySimple
	
	/**The used storages*/
	private Map<String,Double> usedStorages;
	
	/**The free storages*/
	private List<Double> freeStorages;
	

	/**
	 * Creates the new ReplicaAllocationPolicySimple object.
	 *
	 * @param list the node list
	 *
	 * @pre $none
	 * @post $none
	 */
	public ReplicaAllocationPolicySimple(List<? extends StorageNode> nodeList) {
		super(nodeList);
		// TODO Auto-generated constructor stub
		setFreeStorages(new ArrayList<Double>());
		for(StorageNode node:getNodeList()){
			getFreeStorages().add(node.getAvailableSpace());
		}
		setReplicaTable(new HashMap<String,StorageNode>());
		setUsedStorages(new HashMap<String,Double>());
	}
	

/**
 * Sets the used storages
 * @author hua hero
 * @param  usedStorages the used storages.
 *
 */
	protected void setUsedStorages(HashMap<String, Double> usedStorages) {
		// TODO Auto-generated method stub
		this.usedStorages = usedStorages;
	}
	/**
	 * Gets the used storages
	 * @return	the used storages
	 */
	protected Map<String,Double> getUsedStorages(){
		return usedStorages;
	}
	/**
	 * Gets the free storages.
	 *
	 * @return the free storges
	 */
	private List<Double> getFreeStorages() {
		// TODO Auto-generated method stub
		return this.freeStorages;
	}
	
	/**
	 * Sets the free storages
	 * @author Hua Hero
	 * @param freeStorages the free storage
	 */
	private void setFreeStorages(List<Double> freeStorages) {
		// TODO Auto-generated method stub
		this.freeStorages =freeStorages;
	}

	/**
	 * Sets the replica table.
	 *
	 * @param replicaTable the replica table
	 */
	protected void setReplicaTable(Map<String, StorageNode> replicaTable) {
		this.replicaTable = replicaTable;
	}
	/**
	 * Gets the replica table.
	 *
	 * @return the replica table
	 */
	public Map<String, StorageNode> getReplicaTable() {
		return replicaTable;
	}
	/**
	 * @author hua hero
	 *Allocate a host for a given BlockReplica.
	 *@param replica BlockReplica specifiction
	 *@return $true if the host could be allocated; $false otherwise
	 *@pre none
	 *@post none
	 */
	@Override
	public boolean allocateNodeForReplica(BlockReplica replica) {
		// TODO Auto-generated method stub
		int requiredStorages = replica.getSize();
		boolean result = false;
		int tries = 0;
		List<Double> freeStoragesTmp = new ArrayList<Double>();
		for (Double freeStorages : getFreeStorages()) {
			freeStoragesTmp.add(freeStorages);
		}

		if (!getReplicaTable().containsKey(replica.getUniId())) { //if this replica was not created
			do {//we still trying until we find a host or until we try all of them
				double moreFree = Double.MIN_VALUE;
				int idx = -1;

				//we want the host with less pes in use
				for (int i=0; i < freeStoragesTmp.size(); i++) {
					if (freeStoragesTmp.get(i) > moreFree) {
						moreFree = freeStoragesTmp.get(i);
						idx = i;
					}
				}

				StorageNode node = getNodeList().get(idx);
//				result = node.vmCreate(vm);
				result = node.replicaCreate(replica);

				if (result) { //if replica were succesfully created in the host
					//Log.printLine("VmAllocationPolicy: VM #"+vm.getVmId()+ "Chosen host: #"+host.getMachineID()+" idx:"+idx);
//					getVmTable().put(vm.getUid(), host);
					getReplicaTable().put(replica.getUniId(), node);
//					getUsedPes().put(vm.getUid(), requiredPes);
					getUsedStorages().put(replica.getUniId(), (double) requiredStorages);
//					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					getFreeStorages().set(idx, getFreeStorages().get(idx)-requiredStorages);
					result = true;
					break;
				} else {
//					freePesTmp.set(idx, Integer.MIN_VALUE);
					freeStoragesTmp.set(idx, Double.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreeStorages().size());

		}

		return result;
	}

	@Override
	public boolean allocateNodeForReplica(BlockReplica replica, StorageNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deallocateNodeForReplica(BlockReplica replica) {
		// TODO Auto-generated method stub

	}

	@Override
	public StorageNode getNode(BlockReplica replica) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageNode getNode(int nodeId, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends BlockReplica> replicaList) {
		// TODO Auto-generated method stub
		return null;
	}

}
