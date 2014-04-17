/*
 * Title:        CloudSim Toolkit CloudStorage Extension
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013-2014 Xiamen University,China
 */
package cloudStorage;


import org.cloudbus.cloudsim.ParameterException;

/**
 * BlockReplica represents a replica of a block: it stores inside a Host,
 * sharing hostList with other replicas. It processes cloudlets(read or write).
 * This processing happens according to a policy, defined by the
 * CloudletScheduler. Each replica has a owner, which can submit cloudlets to
 * the host to be stored,deleted and modified
 * 
 * @author HuaHero(huahero.long@gmail.com)
 * 
 * @since CloudSim Toolkit 2.1
 */

public class BlockReplica extends Block{
	/**The number of Data Center storing  this data*/
	public int SelectedDC;

	public StorageNode storageNode;
	/**ReplicaScheduler是负责数据的读、写任务*/
	public ReplicaScheduler scheduler;
	/** The ram.读写副本(数据)需要多少内存还真的不好定 *//*
	private int ram;
	
	public BlockReplica(int id, String uid, String blkRepName,
			boolean recentlyCreated, int id2,
			CloudletScheduler cloudletScheduler, StorageNode nodeHost,
			boolean inMigration, boolean recentlyCreated2) throws ParameterException {
		super(id, blkRepName, recentlyCreated);
		id = id2;
		this.cloudletScheduler = cloudletScheduler;
		this.nodeHost = nodeHost;
		this.inMigration = inMigration;
		recentlyCreated = recentlyCreated2;
	}

	/**
	 * Creates a new BlockReplica object.
	 * 
	 * @param id
	 *            unique ID of the replica
	 * @param userId
	 *            ID of the replica's owner
	 * @param rep_block
	 *            the rep_block of block
	 * @param cloudletScheduler
	 *            cloudletScheduler policy for cloudlets
	 * @param priority
	 *            the priority
	 
	 * @pre id >= 0
	 * @pre userId >= 0
	 * @pre size > 0
	 * @pre ram > 0
	 * @pre bw > 0
	 * @pre cpus > 0
	 * @pre priority >= 0
	 * @pre cloudletScheduler != null
	 * @post $none
	 */
	
	public StorageNode getStorageNode() {
		return storageNode;
	}

	public void setStorageNode(StorageNode storageNode) {
		this.storageNode = storageNode;
	}
	public void setStorageNodeId(final int id){
		this.id = id;
	}
	
	public void setSelectedDC(int selDC){
		this.SelectedDC = selDC;
	}
	public int getSelectedDC(){
		return this.SelectedDC;
	}
	/**
	 * Updates the processing of cloudlets running on this VM.
	 * 
	 * @param currentTime
	 *            current simulation time
	 * @param mipsShare
	 *            array with MIPS share of each Pe available to the scheduler
	 * 
	 * @return time predicted completion time of the earliest finishing
	 *         cloudlet, or 0 if there is no next events
	 * 
	 * @pre currentTime >= 0
	 * @post $none
	 *//*
	public double updateProcessing(double currentTime, List<Double> mipsShare) {
		if (mipsShare != null) {
			return getCloudletScheduler().updateVmProcessing(currentTime,
					mipsShare);
		}
		return 0.0;
	}*/


	
	/**
	 * des:construct the BlockReplica with given id\its userId and its name
	 * @param id the data id
	 * @param userId  the user id
	 * @param replicaName  the data name
	 * @throws ParameterException
	 */
	public BlockReplica(int id, int userId, String replicaName)
			throws ParameterException {
		super(id, userId, replicaName);
		// TODO Auto-generated constructor stub
		//the following like set
		this.getCurrentAllocatedSize();
		this.setSelectedDC(0);
	}

	public BlockReplica(int id,int userId,String replicaName,ReplicaScheduler scheduler) throws ParameterException{
		super(id,userId,replicaName,scheduler);
		this.getCurrentAllocatedSize();
	}
	
	/**
	 * Gets the current allocated size.
	 * 
	 * @return the current allocated size
	 */
	public double getCurrentAllocatedSize() {
		CurrentAllocatedSize = this.getFileAttribute().getFileSize();
		return CurrentAllocatedSize;
	}

}
