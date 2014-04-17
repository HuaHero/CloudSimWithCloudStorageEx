/*
 * Title:        CloudSim Toolkit CloudStorage Extension
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013-2014 Xiamen University,China
 */
package cloudStorage;

import java.util.ArrayList;
import java.util.List;
//import java.util.UUID;

import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.core.CloudSim;


/**
 * CloudFile represents a file(CloudFile) or DataObject: it stores inside a Data
 * Centers but as replica shape,sharing hostList and data centers with other
 * CloudFiles . It processes cloudlets(read or write) with its replica. This
 * processing happens according to a policy, defined by the CloudletScheduler.
 * Each file has a owner, which can submit cloudlets to the host to be
 * stored,deleted and modified
 * 
 * @author HuaHero(huahero.long@gmail.com)
 * 
 * @since CloudSim Toolkit 2.1
 */

/**
 * @author huahero
 * des:CloudFile is an abstract concept of the datas stored in CloudStorage
 */

public class CloudFile extends File{
//	protected static final int blockSize = 64; // 64MB default block size
	protected static final int blockSize = 10000;//5G
	
	protected  double  CurrentAllocatedSize = 0;
	private List<Block> BlockList;

	/** The File id. */
	protected int id;

	/** The user id. */
	protected int userId;

	/**the unique id of the file*/
	protected String uniId;

/*	*//** The MIPS. *//*
	private double mips;

	*//** The PEs number. *//*
	private int pesNumber;

	*//** The ram. *//*
	private int ram;

	*//** The bw. *//*
	private long bw;

	*//** The CloudFilem. */
	
//	 private String CloudFilem;
	 /** In migration flag. */
	
	 protected boolean inMigration;
	 
	
	/** The Cloudlet scheduler. */
	protected ReplicaScheduler cloudletScheduler;

	/** The node list that stored this file. 
	 * this information comes from the Block,and the Block Comes from BlockReplica
	 * */
	private List<StorageNode> storageNodeList;


	/** The recently created. */
	protected boolean recentlyCreated;
	
	/**
	 * des:construct the CloudFile
	 * @param id
	 * @param userId
	 * @param CloudFileName
	 * @param fileSize
	 * @throws ParameterException
	 */
	
	public CloudFile(int id,int userId,String CloudFileName,int fileSize) throws ParameterException{
		super(CloudFileName,fileSize);
		this.id = id;
		this.userId=userId;
		this.setRegistrationID(id);
		this.setResourceID(id);
	}
	
	/**
	 * des:construct the CloudFile
	 * @param fileName
	 * @param fileSize
	 * @param blockList
	 * @param id
	 * @param userId
	 * @param cloudletScheduler
	 * @param storageNodeList
	 * @param recentlyCreated
	 * @throws ParameterException
	 */
	public CloudFile(
			String fileName,
			int fileSize,
			List<Block> blockList,
			int id,
			int userId,
			ReplicaScheduler cloudletScheduler,
			List<StorageNode> storageNodeList,
			boolean recentlyCreated) throws ParameterException {
		super(fileName, fileSize);
		BlockList = blockList;
		this.id = id;
		this.userId = userId;
		this.cloudletScheduler = cloudletScheduler;
		this.storageNodeList = storageNodeList;
		this.recentlyCreated = recentlyCreated;
		this.setRegistrationID(id);
		this.setResourceID(id);
		//with given blocklist,the following means set
		this.getCurrentAllocatedSize();
	}
	

	/**
	 * 初始化数据对象的初始化工作
	 * 这些工作包括：默认为文件没有在迁移、文件拥有者为其用户名id、文件是最近创建、raw数据类型
	 * 现在时间为文件初次事务时间
	 * @date:2014/3/17
	 * @throws ParameterException
	 */
	public void init() throws ParameterException{
		this.setUniId();
		if(!this.isRegistered()){
			Log.printLine("File "+this.getName()+"who's id is "+this.id+" is failed to register");
			return ;
		}
//		this.setBlockList();
		this.setInMigration(false);
		this.setOwnerName(Integer.toString(userId));
		this.setRecentlyCreated(true);
		this.setType(TYPE_RAW_DATA);
		this.setTransactionTime(CloudSim.getSimulationCalendar().getTime().getTime());
		Log.printLine("File "+this.getName()+" who's id is " + this.id + " is inited!");
	}

	/**
	 * Creates a new CloudFileCharacteristics object.
	 * 
	 * @param id
	 *            unique ID of the File
	 * @param userId
	 *            ID of the File's owner
	 * @param size
	 *            amount of storage
	 * @param cloudletScheduler
	 *            cloudletScheduler policy for cloudlets
	 * @param priority
	 *            the priority
	
	 * @pre id >= 0
	 * @pre userId >= 0
	 * @pre size > 0
	 * @pre priority >= 0
	 * @pre cloudletScheduler != null
	 * @post $none
	 */
	public String getUniId() {
		return uniId;
	}
	/*
	 * 
	 */
	public String setUniId(){
		//但本人不太喜欢这个太长的文件名，而且也难以找到其与块、副本的关系 
//		uniId = UUID.randomUUID().toString();//利用mac加timestamp生成的guid,java中
		uniId = name+"-"+id + "-"+userId;
		return uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}
	public boolean isInMigration() {
		return inMigration;
	}
	public void setInMigration(boolean inMigration) {
		this.inMigration = inMigration;
	}

	public static double getBlockSize() {
		return blockSize;
	}

	/*public static void setBlockSize(double blockSize) {
		CloudFile.blockSize = blockSize;
	}*/

	public List<Block> getBlockList() {
		return BlockList;
	}

	/**
	 * des:initiate the blocklist of a  cloudfile autonomy
	 * @throws ParameterException
	 */
	public void setBlockList() throws ParameterException{
		int blockScale = (int)Math.ceil(getFileAttribute().getFileSize()/blockSize);//arg0+1
		for(int i=0;i<blockScale;i++){
			BlockList.add(new Block(i, userId,getName())); //这里与main中建副本重复了工作
		}
	}
	
	/**
	 * des: set the blocklist of a cloud file manually
	 * @param blockList
	 */
	public void setBlockList(List<Block> blockList) {
		BlockList = blockList;
	}

	public List<StorageNode> getStorageNodeList() {
		int blkScale = this.getBlockList().size();  //显然文件至少一块
		for (int i = 0; i < blkScale; i++) {
			int replicaScale = this.getBlockList().get(i).getStorageNodeList()
					.size();
			for (int j = 0; j < replicaScale; j++)
				storageNodeList.add(this.getBlockList().get(i)
						.getStorageNodeList().get(j));
		}
		return storageNodeList;
	}

	/*
	 * just for some one who wants to appoint the StorageNode to store the CloudFile
	 */
	public void setStorageNodeList(List<StorageNode> storageNodeList) {
		this.storageNodeList = storageNodeList;
	}


	/**
	 * Get utilization created by all clouddlets running on this CloudFile.
	 * 
	 * @param time
	 *            the time
	 * 
	 * @return total utilization
	 */
	/*public double getTotalUtilizationOfCpu(double time) {
		return getCloudletScheduler().getTotalUtilizationOfCpu(time);
	}
*/


	/**
	 * Generate unique string identificator of the CloudFile.
	 * 
	 * @param userId
	 *            the user id
	 * @param CloudFileId
	 *            the  CloudFileId
	 * 
	 * @return string uid
	 */
	public static String getUniqueId(int userId, int Id) {
		return userId + "-" + Id;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the user id.
	 * 
	 * @param userId
	 *            the new user id
	 */
	protected void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Gets the ID of the owner of the CloudFile.
	 * 
	 * @return CloudFile's owner ID
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getUserId() {
		return userId;
	}


	/**
	 * Sets the host that runs this CloudFile.
	 * 
	 * @param host
	 *            Host running the CloudFile
	 * 
	 * @pre host != $null
	 * @post $none
	 */
//	public void setHost(Host host) {
//		this.host = host;
//	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	/*public Host getHost() {
		return host;
	}*/

	/**
	 * Gets the CloudFile scheduler.
	 * 
	 * @return the CloudFile scheduler
	 */
//	public CloudletScheduler getCloudletScheduler() {
	public ReplicaScheduler getCloudletScheduler(){
		return cloudletScheduler;
	}

	/**
	 * Sets the CloudFile scheduler.
	 * 
	 * @param cloudletScheduler
	 *            the new CloudFile scheduler
	 */
	protected void setCloudletScheduler(ReplicaScheduler cloudletScheduler) {
		this.cloudletScheduler = cloudletScheduler;
	}

	
	
	/**
	 * Gets the current allocated size.
	 * 
	 * @return the current allocated size
	 */
	public double getCurrentAllocatedSize() {
		int blkScale = this.getBlockList().size();
		
		for(int i=0;i<blkScale;i++){
			CurrentAllocatedSize+=this.getBlockList().get(i).getCurrentAllocatedSize();
		}
		return this.CurrentAllocatedSize;
	}



	/**
	 * Checks if is recently created.
	 * 
	 * @return true, if is recently created
	 */
	public boolean isRecentlyCreated() {
		return recentlyCreated;
	}

	/**
	 * Sets the recently created.
	 * 
	 * @param recentlyCreated
	 *            the new recently created
	 */
	public void setRecentlyCreated(boolean recentlyCreated) {
		this.recentlyCreated = recentlyCreated;
	}
	
	/**
	 * Gets the current requested storages.
	 * @date 2014/3/22
	 * @return the current requested storages
	 */
	public List<Double> getCurrentRequestedStorages() {
		List<Double> currentRequestedStorages = getCloudletScheduler().getCurrentRequestedStorages();

		if (isRecentlyCreated()) {
			boolean storagesIsNull = true;
			for (double storages : currentRequestedStorages) {
				if (storages > 0.0) {
					storagesIsNull = false;
					setRecentlyCreated(false);
					break;
				}
			}

			//if (mipsIsNull && isRecentlyCreated()) {
			if (storagesIsNull) {
				currentRequestedStorages = new ArrayList<Double>();
				/*for (int i = 0; i < getPesNumber(); i++) {
					currentRequestedMips.add(getMips());
				}*/
				currentRequestedStorages.add((double)getSize());
			}
		}

		return currentRequestedStorages;
	}

}
