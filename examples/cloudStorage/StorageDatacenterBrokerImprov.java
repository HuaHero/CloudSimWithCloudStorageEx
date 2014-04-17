package cloudStorage;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.CloudletList;

/**
 * @author huahero
 * @description 为了防止对原CloudSim-2-1-1中相应的源代码做太多改动，所以尽量建立新文件，
 *              在继承原来需要的类的基础上，加入自己的新的东西
 *              这里，在原来的代理基类基础上，加入了对数据集属性，并可以设置代理对指定的数据中心的监视，以及对相应属性的初始化等工作
 * @date 2014/3/24
 */

public class StorageDatacenterBrokerImprov extends DatacenterBroker {

	/** The replicas list */
	// private List<? extends BlockReplica> replicaList;
	protected List<? extends BlockReplica> replicaList;

	/** The storage node list */
	// private List<? extends StorageNode> nodeList;
	protected List<? extends StorageNode> nodeList;

	// 经过TOPSIS算法对节点进行排序后的节点按TOPSIS排序的节点号序列
	// private int[] rankedTOPSISnodeIndex;
//	protected int[] rankedTOPSISnodeIndex;
	/** The datacenter list */
	// private List<StorageDatacenter> datacenterList;
	protected List<StorageDatacenter> datacenterList;
	
	/**经过AHP-逆向云算法排序的数据中心索引号*/
//	private int[] rankedDCindex; 
	
	/**the MAX number of Data Center each data can be replicated or stored*/
	protected static final short MAX_DC = 5;
	
	private static final int numDc = 5;
	
	public List<Integer> selDcIndex; 

	public StorageDatacenterBrokerImprov(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
		setReplicaList(new ArrayList<BlockReplica>());
		// getDatacenterList(); //actually this is not working when the
		// construct of SDC haven't finished
		setNodeList(new ArrayList<StorageNode>());

		// setNodeList();
	}

	/**
	 * Gets the datacenter list with datacenter ids
	 * 
	 * @return
	 */
	public List<StorageDatacenter> getDatacenterList() {
		int datacenterSize = this.getDatacenterIdsList().size();
		datacenterList = new ArrayList<StorageDatacenter>();
		for (int i = 0; i < datacenterSize; i++) {
			int datacenterId = this.getDatacenterIdsList().get(i);
			StorageDatacenter datacenter = (StorageDatacenter) CloudSim
					.getEntity(datacenterId);
			datacenterList.add(datacenter);
		}
		return datacenterList;
	}

	/*
	 * public void setDatacenterList(List<? extends StorageDatacenter>
	 * datacenterList) { this.datacenterList = datacenterList; }
	 */

	/**
	 * Sets the replica list manually.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param replicaList
	 *            the new replica list
	 */
	public void setReplicaList(List<? extends BlockReplica> replicaList) {
		this.replicaList = replicaList;
	}

	/**
	 * des:sets the node list manually
	 * 
	 * @param nodeList
	 */
	public void setNodeList(List<? extends StorageNode> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * This method is used to send to the broker the data list that must be
	 * created.And also the broker will allocate the data according special data
	 * distribution method
	 * 
	 * @param replicaList
	 *            the list
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitReplicaList(List<? extends BlockReplica> replicaList) {
		// TODO Auto-generated method stub
		getReplicaList().addAll(replicaList);
		// this.bindReplicasToStorageNodesSimple();
//		this.bindReplicasToStorageNodesSimple_Net();
//		 this.bindReplicasToStorageNodesTOPSIS();
//		this.bindReplicasToStorageNodesTOPSIS_Net();
//		this.bindReplicasToStorageNodeRand();
		//this.bindReplicasToStorageNodeRand_Net();
		
//		this.bindReplicasToStorageNode_DcSelectAHP_Net();
	}

	/**
	 * Set the broker's monitoring datacenter,this is must be called before
	 * calling bindReplicasToStorageNodesSimple()
	 * 
	 * @param datacenterList
	 */
	@SuppressWarnings("unchecked")
	public <T extends StorageDatacenter> void setDatacenters(
			List<T> datacenterList) {
		// TODO Auto-generated method stub
		this.datacenterList = (List<StorageDatacenter>) datacenterList;
		
		/**configure network before we begin to add file to the Storage Cloud*/
//		NetworkTopology.buildNetworkTopology("examples/org/cloudbus/cloudsim/examples/network/topology.brite");
		//maps CloudSim entities to BRITE entities
		//PowerDatacenter datacenter_i will correspond to BRITE node i
		/*double[] accrossLatency = {144.0,89.0,12.0,90.0,123.0};
		for(int i=0;i<numDc;i++){
			int dcId = datacenterList.get(i).getId();
			//addLink between broker and datacenter
			NetworkTopology.addLink(dcId,this.getId(),10.0,accrossLatency[i]);
		}*/
		
		setNodeList();
	}

	/**
	 * des:Sets the node list autonomy according to the Datacenters and their
	 * characteristics
	 */
	protected void setNodeList() {
		int datacenterNum = this.datacenterList.size();
		for (int i = 0; i < datacenterNum; i++) {
			// why not this.nodeList.addAll();
			getNodeList()
					.addAll((List<? extends StorageNode>) datacenterList.get(i).nodeList);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends StorageNode> List<T> getNodeList() {
		return (List<T>) this.nodeList;
	}

	/**
	 * 
	 * @return the datas this broker responsible for
	 */
	@SuppressWarnings("unchecked")
	protected <T extends BlockReplica> List<T> getReplicaList() {
		return (List<T>) this.replicaList;
	}

	/**
	 * des:allocate the cloudlet to special replica
	 * 
	 * @param CloudletId
	 * @param replicaId
	 */
	public void bindCloudletToReplica(int CloudletId, int replicaId) {
		// ReplicaCloudletList.
		CloudletList.getById(this.getCloudletList(), CloudletId).setVmId(
				replicaId);
	}

	/**
	 * store data in the data center's nodes according the TOPSIS( Technique for
	 * Order Preference by Similarity to Ideal Solution)
	 * 
	 * @param none
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void bindReplicasToStorageNodesTOPSIS() {
		// TODO Auto-generated method stub
		int replicaNum = replicaList.size();
		int nodeSize = nodeList.size();
		int idx = 0;
		for (int i = 0; i < replicaNum; i++) {
//			TOPSIS();
			TOPSIS.buildTOPSIS(datacenterList, (List<StorageNode>) nodeList);
			BlockReplica rep = replicaList.get(i);
			double replicaSize = rep.getSize();
//			StorageNode node = nodeList.get(this.rankedTOPSISnodeIndex[idx]);// rankedTOPSISnodeIndex中对于小数据不行呢
			StorageNode node = nodeList.get(TOPSIS.rankedTOPSISnodeIndex[idx]);
			if (node.getAvailableSpace() - replicaSize > 0.000000000000000001
					&& !node.contains(rep.getName())) {
				double time = node.addFile(rep);// if the node has already added
												// this file,the time just
												// include networkLatency and
												// transport time
				Log.printLine("it take " + time
						+ " seconds to write the replica #" + rep.getId()
						+ "to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());
			} else {// rep没有加进去
				i--;
			}
			idx = (idx + 1) % nodeSize;
		}

	}

	/*
	 * public void qsort(double R[],int n){ double pivot = R[0]; }
	 */
	/**
	 * store the data in the data center's nodes just by first fit first service
	 */
	public void bindReplicasToStorageNodesSimple() {
		int replicaNum = replicaList.size();
		nodeList = this.getNodeList();
		int nodeNum = nodeList.size();
		int idx = 0;
		for (int i = 0; i < replicaNum; i++) {
			StorageNode node = nodeList.get(idx);
			// BlockReplica replica = replicaList.remove(i);
			BlockReplica replica = replicaList.get(i);
			double replicaSize = replica.getSize();
			if (node.getAvailableSpace() - replicaSize >= 0.000000000000000000000001
					&& !node.contains(replica.getName())) {
				double time = node.addFile(replica);
				Log.printLine("it take " + time
						+ " seconds to write the replica #" + replica.getId()
						+ "to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());
				// node.setCapacity(node.getCapacity()-replicaSize);
				idx = (idx + 1) % nodeNum;
			} else {
				idx = (idx + 1) % nodeNum;
				i--;// 该副本还未分配到node上，故重新再看接下来的node能否有空间存副本
			}
		}
	}

	/**
	 * Store data to StorageNode with the Node is selected randomly
	 */
	public void bindReplicasToStorageNodeRand() {
		int replicaNum = replicaList.size();
		int nodeSize = nodeList.size();
		for (int i = 0; i < replicaNum; i++) {
			BlockReplica replica = replicaList.get(i);
			StorageNode node = nodeList
					.get(java.util.concurrent.ThreadLocalRandom.current()
							.nextInt(nodeSize));
			double replicaSize = replica.getSize();
			if (node.getAvailableSpace() - replicaSize >= 0.000000000000000000000001
					&& !node.contains(replica.getName())) {
				double time = node.addFile(replica);
				Log.printLine("it take " + time
						+ " seconds to write the replica #" + replica.getId()
						+ "to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());
			} else {
				i--;
			}
		}
	}
	
	
	/////////////Add or Storage the File in the Stroage Datacenter with considering the network/////////////////////
	/**
	 * store data in the data center's nodes according the TOPSIS( Technique for
	 * Order Preference by Similarity to Ideal Solution)
	 * 
	 * @param none
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void bindReplicasToStorageNodesTOPSIS_Net() {
		// TODO Auto-generated method stub
		int replicaNum = replicaList.size();
		int nodeSize = nodeList.size();
		int idx = 0;
		for (int i = 0; i < replicaNum; i++) {
			TOPSIS.buildTOPSIS(this.datacenterList, (List<StorageNode>) this.nodeList);
			BlockReplica rep = replicaList.get(i);
			double replicaSize = rep.getSize();
			// rankedTOPSISnodeIndex中对于小数据不行呢
			StorageNode node = nodeList.get(TOPSIS.rankedTOPSISnodeIndex[idx]);
			if (node.getAvailableSpace() - replicaSize > 0.000000000000000001
					&& !node.contains(rep.getName())) {
				double time = node.addFile(rep);// if the node has already added
												// this file,the time just
												// include networkLatency internal and
												// transport time
				double accrossLatency = NetworkTopology.getDelay(getId(), node.getDatacenter().getId()) ;
				time += accrossLatency;
				/*Log.printLine("it take " + time
						+ " seconds to write the replica #" + rep.getId()
						+ "to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());*/
				Log.printLine("replica #" + rep.getId()+"    			"
						+ node.getDatacenter().getName() + " node #"
						+ node.getId()+" 			"+time);
			} else {// rep没有加进去
				i--;
			}
			idx = (idx + 1) % nodeSize;
		}

	}
	
	/**
	 * store the data in the data center's nodes just by first fit first service 
	 */
	public void bindReplicasToStorageNodesSimple_Net() {
		int replicaNum = replicaList.size();
		nodeList = this.getNodeList();
		int nodeNum = nodeList.size();
		int idx = 0;
		for (int i = 0; i < replicaNum; i++) {
			StorageNode node = nodeList.get(idx);
			// BlockReplica replica = replicaList.remove(i);
			BlockReplica replica = replicaList.get(i);
			double replicaSize = replica.getSize();
			if (node.getAvailableSpace() - replicaSize >= 0.000000000000000000000001
					&& !node.contains(replica.getName())) {
				double time = node.addFile(replica);
				double accrossLatency = NetworkTopology.getDelay(getId(), node.getDatacenter().getId()) ;
				time += accrossLatency;
				Log.printLine("it take " + time
						+ " seconds to write the replica #" + replica.getId()
						+ " to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());
				// node.setCapacity(node.getCapacity()-replicaSize);
				idx = (idx + 1) % nodeNum;
			} else {
				idx = (idx + 1) % nodeNum;
				i--;// 该副本还未分配到node上，故重新再看接下来的node能否有空间存副本
			}
		}
	}
	
	/**
	 * Store data to StorageNode with the Node is selected randomly
	 */
	public void bindReplicasToStorageNodeRand_Net() {
		int replicaNum = replicaList.size();
		int nodeSize = nodeList.size();
		for (int i = 0; i < replicaNum; i++) {
			BlockReplica replica = replicaList.get(i);
			StorageNode node = nodeList
					.get(java.util.concurrent.ThreadLocalRandom.current()
							.nextInt(nodeSize));
			double replicaSize = replica.getSize();
			if (node.getAvailableSpace() - replicaSize >= 0.000000000000000000000001
					&& !node.contains(replica.getName())) {
				double time = node.addFile(replica);
				double accrossLatency = NetworkTopology.getDelay(getId(), node.getDatacenter().getId()) ;
				time += accrossLatency;
				/*Log.printLine("it take " + time
						+ " seconds to write the replica #" + replica.getId()
						+ " to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());*/
				Log.printLine("replica #" + replica.getId()+"    			"
						+ node.getDatacenter().getName() + " node #"
						+ node.getId()+" 			"+time);
			} else {
				i--;
			}
		}
	}
	
	/**
	 * Store data to StorageNode with the Node is selected based on backward-cloud 
	 * generator and AHP
	 */
	public void bindReplicasToStorageNode_DcSelectAHP_Net() {
		int replicaNum = replicaList.size();
//		int numDc = datacenterList.size();
		//这里等于先赋值为0的话，对后面选择数据中心索引有误导
		/*rankedDCindex = new int[numDc];
		for(int i=0;i<numDc;i++){
			rankedDCindex[i] = 0;
		}*/
		
		int dcIndex = 0,nodeId=0;
		for (int i = 0; i < replicaNum; i++) {
			BlockReplica replica = replicaList.get(i);
			
			//采用AHP-逆向云算法生成排序好了的数据中心索引
			AHP_BackwardCloud.AHP_BackwardCloud_Init(datacenterList);
			dcIndex = dcIndex % AHP_BackwardCloud.rankedDCindex.length;
//			int localNodeId =TOPSIS_Local(datacenterList.get(AHP_BackwardCloud.rankedDCindex[dcIndex]));
			TOPSIS_Local(datacenterList.get(AHP_BackwardCloud.rankedDCindex[dcIndex]));
			nodeId = nodeId % TOPSIS.rankedTOPSISnodeIndex.length;
			int localNodeId = TOPSIS.rankedTOPSISnodeIndex[nodeId];
			double replicaSize = replica.getSize();
			StorageDatacenter dc =  datacenterList.get(AHP_BackwardCloud.rankedDCindex[dcIndex]);
			List<StorageNode> localNodeLst = (List<StorageNode>) dc.nodeList;
			StorageNode node = dc.NodeList.getById(localNodeLst, localNodeId);
			if (node.getAvailableSpace() - replicaSize >= 0.000000000000000000000001
					&& !node.contains(replica.getName())) {
				double time = node.addFile(replica);
				double accrossLatency = NetworkTopology.getDelay(getId(), node.getDatacenter().getId()) ;
				time += accrossLatency;
				/*Log.printLine("it take " + time
						+ " seconds to write the replica #" + replica.getId()
						+ " to be stored in datacenter "
						+ node.getDatacenter().getName() + " node #"
						+ node.getId());*/
				Log.printLine("replica #" + replica.getId()+"    			"
						+ node.getDatacenter().getName() + " node #"
						+ node.getId()+" 			"+time);
			} else {
				i--;
			}
			dcIndex ++;
			nodeId ++;
		}
	}
	
	/**
	 * 在某一个数据中心中执行TOPSIS()算法
	 * in data center,according the TOPSIS strategy,rank the node id
	 *  */
	@SuppressWarnings("unchecked")
	public void TOPSIS_Local(StorageDatacenter dc){
//		int firstId = 0;
		List<StorageDatacenter> dcList = new ArrayList<StorageDatacenter>();
		dcList.add(dc);
		List<StorageNode> nodeLst = new ArrayList<StorageNode>();
		nodeLst = (List<StorageNode>) dc.nodeList;
		TOPSIS.buildTOPSIS(dcList, nodeLst);
//		firstId = TOPSIS.rankedTOPSISnodeIndex[0];
//		return firstId;
	}
	
	/**
	 * Read the given data from the storage node and log the time it cost
	 * @param replica the data that wants to read
	 */
	public void readData(BlockReplica replica){
		double time = 0.0;
		int nodeNum = nodeList.size();
		for(int i=0;i<nodeNum;i++){
			StorageNode node = nodeList.get(i);
			if(node.contains(replica)){
				time += replica.getSize() /node.getMaxTransferRate();
				break;
			}
		}
		Log.printLine("read replica #"+replica.getId()+" cost: "+time+" secnods");
	}
}
