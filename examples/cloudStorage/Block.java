/*
 * Title:        CloudSim Toolkit CloudStorage Extension
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013-2014 Xiamen University,China
 */
package cloudStorage;

import java.util.List;

//import org.apache.commons.lang3.StringUtils;
import org.cloudbus.cloudsim.ParameterException;



/**
 * Block represents a block of a file(CloudFile) or DataObject: it stores inside a Host but
 * as replica shape,sharing hostList and data centers with other blocks . 
 * It processes cloudlets(read or write) with its replica.
 * This processing happens according to a policy, defined by the
 * CloudletScheduler. Each file has a owner, which can submit cloudlets to
 * the host to be stored,deleted and modified
 * 
 * @author HuaHero(huahero.long@gmail.com)
 * 
 * @since CloudSim Toolkit 2.1
 */


/**
 * @author lenovo
 *
 */
public class Block extends CloudFile{
	
	private List<BlockReplica> ReplicaList;
	/**The number of Data Center storing  this data*/
	public int SelectedDC;
	protected int replicaNum;	//冗余度，复制因子，在本工程中，以块作为复制单位
	/** The storage node used to store the replicas of this Block,
	 * the information comes from the replica's report
	 *  */
//	private List<StorageNode> nodeList;   //extend from super class

	@SuppressWarnings("unused")
	private List<StorageNode> storageNodeList;
	/** The current allocated size.from the report of BlockReplica */
//	private double currentAllocatedSize;


	/**
	 * des:construct the Block with given id,user id and the name
	 * @param id the given id
	 * @param userId the user id
	 * @param blkName the block name
	 * @throws ParameterException
	 */
	public Block(int id,int userId,String blkName) throws ParameterException{
		super(id,userId,blkName,blockSize);
		this.getCurrentAllocatedSize();
		this.SelectedDC = 1;
	}
	public Block(int id,int userId,String blkName,int replicaNum,boolean recentlyCreated) throws ParameterException{
		//super(StringUtils.split(blkName, "-")[0],blockSize);//有待完状善
		super(id,userId,blkName,blockSize);
		this.recentlyCreated = recentlyCreated;
		this.replicaNum = replicaNum;
		this.getCurrentAllocatedSize();
		this.SelectedDC = 1;
	}
	public Block(int id, int userId, String blkName,ReplicaScheduler cloudletScheduler,
			List<StorageNode> nodeList,
			boolean inMigration, double currentAllocatedSize,int replicaNum,
			boolean recentlyCreated) throws ParameterException {
		super(id, userId,blkName, blockSize);
		this.id = id;
		this.userId = userId;
		this.cloudletScheduler = cloudletScheduler;
		this.storageNodeList = nodeList;
		this.inMigration = inMigration;
		this.CurrentAllocatedSize = currentAllocatedSize;
		this.recentlyCreated = recentlyCreated;
		this.replicaNum = replicaNum;
		
		this.SelectedDC = 1;
	}

	public Block(int id,int userId,String blkName,List<BlockReplica> replicaList,boolean recentlyCreated) throws ParameterException{
		super(id, userId, blkName, blockSize);
		this.recentlyCreated = recentlyCreated;
		this.ReplicaList = replicaList;
		// with given replica list,the following means set
		this.getCurrentAllocatedSize();
		
		this.SelectedDC = 1;
	}

	
	/**
	 * des:construct the Block with given Id、given userId、given replicaName、scheduler;
	 * the scheduler just stand for data scheduler,which responds to the read and write of data,not only replica
	 * @param id
	 * @param userId
	 * @param replicaName
	 * @param scheduler
	 */
	public Block(int id, int userId, String blkName,
			ReplicaScheduler scheduler) throws ParameterException{
		// TODO Auto-generated constructor stub
		super(id,userId,blkName,blockSize);
		this.getCurrentAllocatedSize();
		
		this.SelectedDC = 1;
	}
	/**
	 * Creates a new BlockCharacteristics object.
	 * 
	 * @param id
	 *            unique ID of the block
	 * @param userId
	 *            ID of the Block's owner
	 * @param size
	 *            amount of storage
	 * @param cloudletScheduler
	 *            cloudletScheduler policy for cloudlets(read or write)
	 * @param priority
	 *            the priority

	 * 
	 * @pre id >= 0
	 * @pre userId >= 0
	 * @pre size > 0
	 * @pre priority >= 0
	 * @pre cloudletScheduler != null
	 * @post $none
	 */
	/*
	 * (non-Javadoc)初始化块，至于块的冗余数，默认为1，如果要改变冗余度，则应在调用init()前调用setReplicaNum()
	 * 
	 * 至于设置CloudletScheduler
	 *如果改变了冗余度，则在调用setReplicaNum()之后也还应调用setReplicaList()
	 */
	public void init() throws ParameterException{
//		this.setBlockList();
		super.init();  //main中因为从这里进去导致做了重复工作
		/**
		 * 或许下面两个方法不放到这里还好些
		 */
		
		/*this.setReplicaList();
		this.setReplicaNum(replicaNum);*/
	}
	public int getReplicaNum() {
		return replicaNum;
	}
	public void setReplicaNum(int replicaNum) {
		this.replicaNum = replicaNum;
	}
	/**
	 * Updates the processing of cloudlets running on this Block.
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
	 */
	/*public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		if (mipsShare != null) {
			return getCloudletScheduler().updateVmProcessing(currentTime,
					mipsShare);
		}
		return 0.0;
	}
*/
	
	/**
	 * des:initiate the replica list of a block autonomy
	 */
	public void setReplicaList(){ //该返回什么类型，还有待仔细分析之后
		for(int i=0;i<replicaNum;i++){
			BlockReplica replica=(BlockReplica) this.makeReplica();
			replica.setName(name+"-rep"+i);
			replica.setId(i);
			replica.setUserId(userId);
			ReplicaList.add(replica);
		}
	}
	public List<BlockReplica> getReplicaList() {
		return ReplicaList;
	}
	
	/***
	 * des:initiate the replica list of a block manually
	 * @param replicaList
	 */
	public void setReplicaList(List<BlockReplica> replicaList) {
		ReplicaList = replicaList;
	}


	/**
	 * Gets the current allocated size.
	 * 
	 * @return the current allocated size
	 */
	public double getCurrentAllocatedSize() {
		 int replicaScale= this.ReplicaList.size();
		 for(int i=0;i<replicaScale;i++){
			 CurrentAllocatedSize+=this.ReplicaList.get(i).getFileAttribute().getFileSize();
		 }
		return CurrentAllocatedSize;
	}


}
