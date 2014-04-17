package cloudStorage;

/*
 * Title:        CloudSim Toolkit CloudStorage Extension
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013-2014 Xiamen University,China
 */


//package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.SanStorage;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * StorageNode class extends a Machine to support storage data 
 * to support simulation of virtualized grids. It executes actions related
 * to management of virtual machines (e.g., creation and destruction). A host has
 * a defined policy for provisioning memory and bw, as well as an allocation policy
 * for Pe's to virtual machines.
 *
 * A host is associated to a datacenter. It can host virtual machines.
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class StorageNode extends SanStorage{
	/** The id. */
	private int id;
	
	/*storage capacity*/
	private double capacity; 
	
	/*network latency*/
	double latency;
	
	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy. */
//	private ReplicaScheduler replicaScheduler;

	/** The vm list. */
	private List<? extends BlockReplica> replicaList;

	/** The pe list. */
	private List<? extends Pe> peList;

    /** Tells whether this machine is working properly or has failed. */
    private boolean failed;

	/** The replicas migrating in. */
	private List<BlockReplica> replicasMigratingIn;
	
	/** The datacenter where the node is placed */
	private StorageDatacenter sdDatacenter;
	
	/**the node scheduler*/
	private ReplicaScheduler cloudletScheduler;
	
	//2014/3/25 For to experiment the YunHai OS replica distribution
	private double IOPS;
	private double Throughput;
	
	/*
	 * initate a StorageNode
	 */
	public StorageNode(double capacity, double bandwidth, double networkLatency)
			throws ParameterException {
		super(capacity, bandwidth, networkLatency);
		// TODO Auto-generated constructor stub
	}
	/*
	 * @par capacity-in MByte
	 * 
	 */
	public StorageNode(int id,double capacity,double bandwidth,double networkLatency,RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,List<? extends Pe> peList) throws ParameterException{
		super(capacity,bandwidth,networkLatency);
		this.id=id;
		this.capacity = capacity;
		this.ramProvisioner = ramProvisioner;
		this.bwProvisioner = bwProvisioner;
		this.peList = peList;
//		this.sdDatacenter = sdDatacenter;
	}

	public StorageNode(int id,double capacity, double bandwidth,
			double networkLatency, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
			/*ReplicaScheduler replicaScheduler,*/
			List<? extends BlockReplica> replicaList,
			List<? extends Pe> peList/*, boolean failed,*/
			/*List<BlockReplica> replicasMigratingIn,*/ /*Datacenter datacenter*/)
			throws ParameterException {
		super(capacity, bandwidth, networkLatency);
		this.id = id;
		this.capacity = capacity;
		this.ramProvisioner = ramProvisioner;
		this.bwProvisioner = bwProvisioner;
//		this.replicaScheduler = replicaScheduler;
		this.replicaList = replicaList;
		this.peList = peList;
//		this.failed = failed;
//		this.replicasMigratingIn = replicasMigratingIn;
//		this.sdDatacenter = sdDatacenter;
	}

	/**
	 * des:construct the StorageNode with the parameters
	 * @param nodeId the node id
	 * @param ramProvisionerSimple the RamProvisioner
	 * @param bwProvisionerSimple	the BwProvisioner
	 * @param storage	the storage needed
	 * @param peList2  the Pes
	 * @param replicaScheduler
	 * @param networkLatency
	 * @throws ParameterException 
	 */
	public StorageNode(int nodeId, RamProvisioner ramProvisionerSimple,
			BwProvisioner bwProvisionerSimple, double storage,
			List<Pe> peList2, ReplicaScheduler replicaScheduler,int bandwidth,
			double networkLatency) throws ParameterException {
		// TODO Auto-generated constructor stub
		super(storage,bandwidth,networkLatency);
		this.id = nodeId;
		this.capacity = storage;
		this.ramProvisioner = ramProvisionerSimple;
		this.bwProvisioner = bwProvisionerSimple;
		this.peList = peList2;
		this.cloudletScheduler = replicaScheduler;
//		this.maxTransferRate =networkLatency;
	}
	/**
	 * Construct StorageNode with id,ramProvisioner,bwProvisioner,storage capacity,PE list,bandwidth and networkLatency
	 * @param nodeId the node id
	 * @param ramProvisionerSimple
	 * @param bwProvisionerSimple
	 * @param storage  storage capacity
	 * @param peList2
	 * @param bandwidth
	 * @param networkLatency
	 * @throws ParameterException 
	 */
	public StorageNode(int nodeId, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, double storage,
			List<Pe> peList2,int bandwidth,double networkLatency) throws ParameterException {
		// TODO Auto-generated constructor stub
		super(storage,bandwidth,networkLatency);
		this.id = nodeId;
		this.capacity = storage;
		this.ramProvisioner = ramProvisioner;
		this.bwProvisioner = bwProvisioner;
		this.peList = peList2;
	}
	
	
	/**
	 * date:2014/3/25
	 * Construct StorageNode with ....
	 * 理论上，IOPS=每秒IO次数=1000ms/(Tseek+Trotaion),CloudSim中默认取Trotaion=4.17，即60*1000/7200/2，Tseek=9ms
	 * @param nodeId
	 * @param ramProvisioner
	 * @param bwProvisioner
	 * @param capactity
	 * @param peList2
	 * @param bandwidth
	 * @param networkLatency
	 * @param IOPS2
	 * @param Throughput2
	 * @throws ParameterException 
	 */
	public StorageNode(int nodeId, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, double capacity, List<Pe> peList2,
			int bandwidth, double networkLatency, double IOPS2,
			double Throughput2) throws ParameterException {
		super(capacity,bandwidth,networkLatency);
		this.avgSeekTime = this.Throughput=Throughput2;
		this.id = nodeId;
		this.capacity = capacity;
		this.ramProvisioner = ramProvisioner;
		this.bwProvisioner = bwProvisioner;
		this.peList = peList2;
		
		this.IOPS = IOPS2;  //theoretically this is equal to 1000ms/(seek time + disk rotation latency)
	
	}
	/**
	 * Construct the StorageNode with given 
	 * @param nodeId
	 * @param ramProvisioner
	 * @param bwProvisioner
	 * @param capacity
	 * @param peList2
	 * @param bandwidth
	 * @param networkLatency
	 * @param avSeekTime
	 * @param transferRate
	 * @param trotationLatency the disk rotation latency
	 * @param Throughput2
	 * @throws ParameterException
	 */
	public StorageNode(int nodeId, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, double capacity, List<Pe> peList2,
			int bandwidth, double networkLatency, double avSeekTime,double transferRate,double trotationLatency,
			double Throughput2) throws ParameterException {
		super(capacity,bandwidth,networkLatency);
		this.avgSeekTime = this.Throughput=Throughput2;
		this.id = nodeId;
		this.capacity = capacity;
		this.ramProvisioner = ramProvisioner;
		this.bwProvisioner = bwProvisioner;
		this.peList = peList2;
		
		this.avgSeekTime = avSeekTime; //in second
		this.maxTransferRate = transferRate;
		this.latency = trotationLatency; //the disk rotation latency
		this.IOPS = 1000/(this.latency + avSeekTime);
	  

	}
	
	public double getLatency() {
		return latency;
	}
	public double getIOPS() {
		return IOPS;
	}
	public double getThroughput() {
		return Throughput;
	}
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public double getCapacity() {
		return capacity;
	}


	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}


	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}


	public void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}


	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}


	public void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	@SuppressWarnings("unchecked")
	public List<BlockReplica> getReplicaList() {
		return (List<BlockReplica>) replicaList;
	}


	public void setReplicaList(List<? extends BlockReplica> replicaList) {
		this.replicaList = replicaList;
	}


	public List<? extends Pe> getPeList() {
		return peList;
	}


	public void setPeList(List<? extends Pe> peList) {
		this.peList = peList;
	}


	public boolean isFailed() {
		return failed;
	}


	public void setFailed(boolean failed) {
		this.failed = failed;
	}


	public List<BlockReplica> getReplicasMigratingIn() {
		return replicasMigratingIn;
	}


	public void setReplicasMigratingIn(List<BlockReplica> replicasMigratingIn) {
		this.replicasMigratingIn = replicasMigratingIn;
	}


	public StorageDatacenter getDatacenter() {
		return sdDatacenter;
	}


	public void setSdDatacenter(StorageDatacenter storageDatacenter) {
		this.sdDatacenter = storageDatacenter;
	}

	/**
     * Sets the particular Pe status on this Machine.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param peId the pe id
     *
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Pe id might not be exist)
     *
     * @pre peID >= 0
     * @post $none
     */
    @SuppressWarnings("unchecked")
	public boolean setPeStatus(int peId, int status) {
        return PeList.setPeStatus((List<Pe>) getPeList(), peId, status);
    }


    /**
	 * Allocates Storagess (and memory) to a new BlockReplica in the Host.
	 *
	 * @param replica BlockReplica being started
	 *
	 * @return $true if the BlockReplica could be stored in the host; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public boolean replicaCreate(BlockReplica replica) {

		if (!/*getRepliaScheduler()*/this.getCloudletScheduler().allocateStoragesForReplica(replica, replica.getCurrentRequestedStorages())) {
			Log.printLine("[VmScheduler.vmCreate] Allocation of Data #" + replica.getId() + " to Host #" + getId() + " failed by Space");
			return false;
		}

		getReplicaList().add(replica);
		replica.setStorageNode(this);
		return true;
	}
	/**
	 * Returns a BlockReplica object.
	 *
	 * @param replicaId the replica id
	 * @param userId ID of replica's owner
	 *
	 * @return the virtual machine object, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public BlockReplica getReplica(int replicaId, int userId){
		for (BlockReplica replica : getReplicaList()) {
			if (replica.getId() == replicaId && replica.getUserId() == userId) {
				return replica;
			}
		}
		return null;
	}

	public void addMigratingInReplica(BlockReplica replica) {
		if (!getReplicasMigratingIn().contains(replica)) {
			getReplicasMigratingIn().add(replica);
		}
	}

	public void removeMigratingInReplica(BlockReplica replica) {
		getReplicasMigratingIn().remove(replica);
	}
	

	/**
	 * @return the node scheduler
	 */
	public ReplicaScheduler getCloudletScheduler() {
		// TODO Auto-generated method stub
		return cloudletScheduler;
	}
	/**
	 * Sets the node scheduler.
	 *
	 * @param cloudletScheduler the new node scheduler
	 */
	protected void setReplicaScheduler(ReplicaScheduler cloudletScheduler) {
		this.cloudletScheduler = cloudletScheduler;
	}

	public double updateReplicasProcessing(double clock) {
		// TODO Auto-generated method stub
		return 0;
	}
}
