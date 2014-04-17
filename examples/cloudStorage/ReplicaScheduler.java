package cloudStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

public class ReplicaScheduler extends CloudletSchedulerSpaceShared/*CloudletScheduler*/{

	/** The current storage share. */
	private List<Double> currentStoragesShare;
	private HashMap<String, List<Double>> storagesMapRequested;
	private List<String> replicasMigratingOut;
	private double availableStorages;
	private int storagesInUse;
	private List<Double> StorageList;
	private Map<String, List<Double>> storagesMap;
	private HashMap<String, List<Double>> storageTable;
	public ReplicaScheduler() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Instantiates a new replica scheduler space ....
	 *
	 * @param storages  the storage list
	 */
	public ReplicaScheduler(double storages) {
		super();
		setStoragesMapRequested(new HashMap<String, List<Double>>());
		setReplicasInMigration(new ArrayList<String>());
		this.currentStoragesShare= new ArrayList<Double>();
		this.currentStoragesShare.add(storages);
		this.StorageList = new ArrayList<Double>();
		this.StorageList.add(storages);
		this.availableStorages=storages;
	}
	/* (non-Javadoc)
	 */
	public boolean allocateStoragesForReplica(BlockReplica replica, List<Double> storagesShareRequested) {
		/**
		 * TODO: add the same to RAM and BW provisioners
		 */
		if (replica.isInMigration()) {
			if (!getReplicasInMigration().contains(replica.getUniId())) {
				getReplicasInMigration().add(replica.getUniId());
			}
		} else {
			if (getReplicasInMigration().contains(replica.getUniId())) {
				getReplicasInMigration().remove(replica.getUniId());
			}
		}
		boolean result = allocateStoragesForReplica(replica.getUniId(), storagesShareRequested);
		updateStorageProvisioning();
		return result;
	}

	/**
	 * Allocate storages for replica.
	 *
	 * @param replicaUid the replica uid
	 * @param storagesShareRequested the storages share requested
	 *
	 * @return true, if successful
	 */
	protected boolean allocateStoragesForReplica(String replicaUid, List<Double> storagesShareRequested) {
		getStoragesMapRequested().put(replicaUid, storagesShareRequested);
		setStoragesInUse(getStoragesInUse() + storagesShareRequested.size());

		double totalRequestedStorages = 0;
		double s = getStorageCapacity();
		for (Double storages : storagesShareRequested) {
			if (storages > s) { // each virtual PE of a VM must require not more than the capacity of a physical PE
				return false;
			}
			totalRequestedStorages += storages;
		}

		List<Double> storagesShareAllocated = new ArrayList<Double>();
		for (Double storagesRequested : storagesShareRequested) {
			if (getReplicasInMigration().contains(replicaUid)) {
				storagesRequested *= 0.9; // performance degradation due to migration = 10% STORAGE
			}
			storagesShareAllocated.add(storagesRequested);
		}

		if (getAvailableStorages() >= totalRequestedStorages) {
			getStoragesMap().put(replicaUid, storagesShareAllocated);
			setAvailableStorages(getAvailableStorages() - totalRequestedStorages);
		} else {
			int storagesSkipped = 0;
			for (List<Double> storagesMap : getStoragesMap().values()) {
				for (int i = 0; i < storagesMap.size(); i++) {
					if (storagesMap.get(i) == 0) {
						storagesSkipped+=storagesMap.get(i).byteValue();
						continue;
					}
				}
			}

			double shortage = (totalRequestedStorages - getAvailableStorages()) / (getStoragesInUse() - storagesSkipped);

			getStoragesMap().put(replicaUid, storagesShareAllocated);
			setAvailableStorages(0);

			double additionalShortage = 0;
			do {
				additionalShortage = 0;
				for (List<Double> storagesMap : getStoragesMap().values()) {
					for (int i = 0; i < storagesMap.size(); i++) {
						if( storagesMap.get(i) == 0) {
							continue;
						}
						if (storagesMap.get(i) >= shortage) {
							storagesMap.set(i, storagesMap.get(i) - shortage);
						} else {
							additionalShortage += shortage - storagesMap.get(i);
							storagesMap.set(i, 0.0);
						}
						if (storagesMap.get(i) == 0) {
//							pesSkipped++;
							continue;
						}
					}
				}
				//SupperessUnchecked!!!!!
				shortage = additionalShortage / (getStoragesInUse() - storagesSkipped);
			} while (additionalShortage > 0);
		}

		return true;
	}
	

	/**
	 * Returns storage capacity in MBytes.
	 *
	 * @return MBytes
	 */
	public double getStorageCapacity() {
		if (getStorageList() == null) {
			Log.printLine("Storage list is empty");
			return 0;
		}
		return getStorageList().get(0).doubleValue();
	}
	/**
	 * Update allocation of Replicas on Storages.
	 */
	protected void updateStorageProvisioning() {
//		Iterator<Double> storageIterator  = getStorageList().iterator();
//		Double storage = storageIterator.next();
//		PeProvisioner peProvisioner = pe.getPeProvisioner();
//		peProvisioner.deallocateMipsForAllVms();
		deallocateStoragesForAllReplicas();
//		double availableMips = peProvisioner.getAvailableMips();
		double avaiStorages = getAvailableStorages();
		for (Map.Entry<String, List<Double>> entry : getStoragesMap().entrySet()) {
			String replicaUid = entry.getKey();
			for (double storages : entry.getValue()) {
				if (avaiStorages >= storages) {
//					peProvisioner.allocateMipsForVm(vmUid, mips);
					allocateStoragesForReplica(replicaUid, storages);
					avaiStorages -= storages;
				} else {
					while (storages >= 0) {
//						peProvisioner.allocateMipsForVm(vmUid, availableMips);
						allocateStoragesForReplica(replicaUid, avaiStorages);
						storages -= avaiStorages;
						if (storages <= 0.1) {
							storages = 0;
							Log.printLine("There is no enough Space (" + storages + ") to accommodate Data " + replicaUid);
							break;
						}
						/*if (!storageIterator.hasNext()) {
							Log.printLine("There is no enough Space (" + storages + ") to accommodate Data " + replicaUid);
						}*/
//						storage = storageIterator.next();
//						peProvisioner = pe.getPeProvisioner();
//						peProvisioner.deallocateMipsForAllVms();
//						deallocateStoragesForAllReplicas();
//						availableMips = peProvisioner.getAvailableMips();
						avaiStorages=getAvailableStorages();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 */
	public boolean allocateStoragesForReplica(BlockReplica replica, double storages) {
		return allocateStoragesForReplica(replica.getUniId(), storages);
	}

	public boolean allocateStoragesForReplica(String replicaUid, double storages) {
		if (getAvailableStorages() < storages) {
			return false;
		}

		List<Double> allocatedStorages;

		if (getStorageTable().containsKey(replicaUid)) {
			allocatedStorages = getStorageTable().get(replicaUid);
		} else {
			allocatedStorages = new ArrayList<Double>();
		}

		allocatedStorages.add(storages);

		setAvailableStorages(getAvailableStorages() - storages);
		getStorageTable().put(replicaUid, allocatedStorages);

		return true;
	}

	protected void setAvailableStorages(double storages) {
		// TODO Auto-generated method stub
		this.availableStorages = storages;
	}

	private HashMap<String, List<Double>> getStorageTable() {
		// TODO Auto-generated method stub
		return storageTable;
	}

	/**
	 * Releases Storages allocated to all the BlockReplicas.
	 * @pre $none
	 * @post $none
	 */
	public void deallocateStoragesForAllReplicas() {
		getStoragesMapRequested().clear();
		setStoragesInUse(0);
	}
	
	/* (non-Javadoc)
	 * @see cloudsim.VmScheduler#deallocateStoragesForReplica
	 */
	public void deallocateStoragesForReplica(BlockReplica replica){
		getStoragesMapRequested().remove(replica.getUniId());
		setStoragesInUse(0);
		getStoragesMap().clear();
		double totalStorage = 0;
		int size = this.StorageList.size();
		for(int i=0;i<size;i++){
			totalStorage += this.StorageList.get(i);
		}
		setAvailableStorages(totalStorage);

//		for (double s : getStorageList()) {
			deallocateStoragesForReplica(replica);
//		}

		for (Map.Entry<String, List<Double>> entry : getStoragesMapRequested().entrySet()) {
			allocateStoragesForReplica(entry.getKey(), entry.getValue());
		}

		updateStorageProvisioning();
	}

	

	
	/**
	 * Gets the storage list.
	 *
	 * @return the storage list
	 */
	@SuppressWarnings("unchecked")
	public  List<Double> getStorageList() {
		return  StorageList;
	}
	
	
	/**
	 * Returns maximum available Storage among all the Storages(nodes's total storage).
	 * For the time shared policy it is just all the available storage.
	 *
	 * @return max storages
	 */
	public double getMaxAvailableStorages() {
		return getAvailableStorages();
	}
	
	/**
	 * Gets the free storages.
	 *
	 * @return the free storages
	 */
	public double getAvailableStorages() {
		return availableStorages;
	}
	/**
	 * Sets the storages in use.
	 *
	 * @param pesInUse the new storages in use
	 */
	protected void setStoragesInUse(int storagesInUse) {
		this.storagesInUse = storagesInUse;
	}

	/**
	 * Gets the storages in use.
	 *
	 * @return the storages in use
	 */
	protected int getStoragesInUse() {
		return storagesInUse;
	}

	/**
	 * Gets the storages map.
	 *
	 * @return the storages map
	 */
	protected Map<String, List<Double>> getStoragesMap() {
		return storagesMap;
	}
	
	/**
	 * Gets the storages map requested.
	 *
	 * @return the storages map requested
	 */
	protected Map<String, List<Double>> getStoragesMapRequested() {
		return storagesMapRequested;
	}
	/**
	 * Sets the storages map requested.
	 *
	 * @param storagesMapRequested the storages map requested
	 */
	private void setStoragesMapRequested(HashMap<String, List<Double>> storagesMapRequested) {
		// TODO Auto-generated method stub
		this.storagesMapRequested = storagesMapRequested;
	}
	
	/**
	 * Gets the replicas in migration.
	 *
	 * @return the replicas in migration
	 */
	protected List<String> getReplicasInMigration() {
		return replicasMigratingOut;
	}
	/**
	 * Sets the replicas in migration.
	 *
	 * @param replicasMigratingOut the new replias in migration
	 */
	protected void setReplicasInMigration(List<String> replicasInMigration) {
		this.replicasMigratingOut = replicasInMigration;
	}
	
	/**
	 * Sets the current storages share.which is the total space can be used to store replicas
	 *
	 * @param currentMipsShare the new current storages share
	 */
	protected void setCurrentStoragesShare(List<Double> currentStoragesShare) {
		this.currentStoragesShare = currentStoragesShare;
	}
	/**
	 * Gets the current storages share.
	 *
	 * @return the current mips share
	 */
	public List<Double> getCurrentStoragesShare() {
		return currentStoragesShare;
	}
	
	public List<Double> getCurrentRequestedStorages() {
		List<Double> storagesShare = new ArrayList<Double>();
		if (getCurrentStoragesShare() != null) {
			for (Double storages : getCurrentStoragesShare()) {
				storagesShare.add(storages);
			}
		}
		return storagesShare;
	}
}
