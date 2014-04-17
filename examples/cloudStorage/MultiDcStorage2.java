package cloudStorage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * <h1>Simulate Cloud Storage in which to study how to place the data(or
 * replica)</h1>
 * 
 * @description: based on
 *               examples/org/cloudbus/cloudsim/examples/CloudSimExample8.java
 *               for to imitate the multi-data center situation,in this source
 *               code,we need to take use of NetworkTopology class and
 *               BlockReplica class(which I created and extended from Block
 *               class,and Block class extended from CloudFile class, and
 *               CloudFile class extended from File class in CloudSim),I will
 *               also use the StorageNode class(which I created and extended
 *               from SanStorage) in this source. for my 2CMSSS(2 Cascading
 *               Master-Slave Storage System) architecture,which is like a
 *               federation cloud but based on HDFS architecture.There must be a
 *               global broker to manage data storage. to simulate
 *               -heterogeneous- heterogeneity of each storage cloud,I give each
 *               storage cloud different composition of storage node
 * This is based on MultiDcStorage.java，and remove those methods that haven't be
 * 	used and added the network simulation
 * @author HuaHero(huahero.long@gmail.com)
 * @date : 2014/3/29
 */
public class MultiDcStorage2 {

	/** The data list */
	private static List<BlockReplica> replicaList;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmList. */
	private static List<Vm> vmList;

	/** The federation clouds's scale,number of datacenter */
	private static short numDC = 2;//5;
	private static List<StorageDatacenter> datacenters;

	/** The block replica / data number */
	private static int numCloudFile = 10;
	private static int numReplica = 3;

	/** the number hosts/storage nodes of each datacenter */
	// private static int[] numNode = {20,15,30,40,55};
	private static int[] numNode = { 2, 1, 3, 4, 5 };

	/** nodes' properties */
	private static int[] mipsLst;
	private static int[] ramLst;
	private static double[] storageCapLst;
	private static int bw = 1000;

	private static double[] avSeekTime;
	private static double[] transferRate;
	private static double[] rotationLatency;
	private static double[] throughPut;

	/**
	 * the rate of each configure node's occurrence in a data center, I suppose
	 * there are 5 different configure situation
	 */
	private static double[][] rates = { { 0.06, 0.2, 0.24, 0.4, 0.1 },
			{ 0.1, 0.1, 0.4, 0.3, 0.1 }, { 0.08, 0.08, 0.54, 0.2, 0.1 },
			{ 0.3, 0.2, 0.1, 0.2, 0.1 }, { 0.2, 0.2, 0.2, 0.2, 0.2 } };
	private static double[] time_zones = { 10.0, 20.0, 30.0, 40.0, 50.0 };

	/**
	 * create the vm accross the the datacenter,so it needs to remember the
	 * createId of vm
	 */
	private static int vmID = 0;

	/** submit */
	/**
	 * init the nodes'mips,ram,storageCapacity property,given the different
	 * configure
	 * 
	 * @param nodeNum
	 *            the number of nodes rate the rate of one configuration
	 *            occurs,the sum of the rate must equal to 1.0 numRate the size
	 *            of the rate array,actually in this program ,I thought there
	 *            are 5 kinds of configuration default
	 */
	public static void initNodesProperty(int nodeNum, double[] rate, int numRate) {
		mipsLst = new int[nodeNum];
		ramLst = new int[nodeNum];
		storageCapLst = new double[nodeNum];
		if (numRate < 5) {
			Log.printLine("the numRate parameter is illegal in calling initNodesProperty");
			return;
		}
		int mipsLst_0 = 750;
		int mipsLst_1 = 1000;
		int mipsLst_2 = 1500;
		int mipsLst_3 = 2250;
		int mipsLst_4 = 2500;
		int ramLst_0 = 512;
		int ramLst_1 = 1024;
		int ramLst_2 = 2048;
		int ramLst_3 = 4096;
		int ramLst_4 = 8192;
		double storageCapLst_0 = 320000.0;
		double storageCapLst_1 = 500000.0;
		double storageCapLst_2 = 1000000.0;
		double storageCapLst_3 = 2000000.0;
		double storageCapLst_4 = 4000000.0;
		// the 1st configuration
		int start = 0;
		int end = (int) Math.floor(nodeNum * rate[0]);
		for (int i = start; i < end; i++) {
			mipsLst[i] = mipsLst_0;
			ramLst[i] = ramLst_0;
			storageCapLst[i] = storageCapLst_0;
		}

		// the 2nd configuration
		start = end;
//		end = start + (int) Math.floor(nodeNum * rate[1]);
		end = (int) Math.floor(nodeNum * (rate[0]+rate[1]));//this is must because nodeNum may very small
		for (int i = start; i <= end; i++) {
			mipsLst[i] = mipsLst_1;
			ramLst[i] = ramLst_1;
			storageCapLst[i] = storageCapLst_1;
		}

		// the 3rd configuration
		start = end + 1;
		end = (int) Math.floor(nodeNum * (rate[0]+rate[1]+rate[2]));
		for (int i = start; i <= end; i++) {
			mipsLst[i] = mipsLst_2;
			ramLst[i] = ramLst_2;
			storageCapLst[i] = storageCapLst_2;
		}
		// the 4th configuration
		start = end + 1;
		end = (int) Math.floor(nodeNum * (rate[0]+rate[1]+rate[2]+rate[3]));
		for (int i = start; i <= end; i++) {
			mipsLst[i] = mipsLst_3;
			ramLst[i] = ramLst_3;
			storageCapLst[i] = storageCapLst_3;
		}

		// the 5th configuration
		start = end + 1;
		end =(int) Math.floor(nodeNum * (rate[0]+rate[1]+rate[2]+rate[3]+rate[4]));
		for (int i = start; i < nodeNum; i++) {
			mipsLst[i] = mipsLst_4;
			ramLst[i] = ramLst_4;
			storageCapLst[i] = storageCapLst_4;
		}
	}

	private static List<Vm> createVM(int userId, int vms, int idShift) {
		// Creates a container to store VMs. This list is passed to the broker
		// later
		LinkedList<Vm> list = new LinkedList<Vm>();

		// VM Parameters
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		// create VMs
		Vm[] vm = new Vm[vms];

		for (int i = 0; i < vms; i++) {
			// VM Parameters
			long size = (long) storageCapLst[i]; // image size (MB)
			int ram = ramLst[i]; // vm memory (MB)
			int mips = mipsLst[i];
			vm[i] = new Vm(idShift + i + vmID, userId, mips, pesNumber, ram,
					bw, size, vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
		}
		vmID += vms;
		return list;
	}

	// //////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 * 
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream oldPrintStream = System.out;
		FileOutputStream bos = new FileOutputStream("MultiDcStorage2BrokerWithNet3.txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(bos),
				oldPrintStream);
		System.setOut(new PrintStream(multi));
		Log.printLine("Starting MultiDcStorage2...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = numDC; // number of grid users,here I just set it
									// equal to datacentr number
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			GlobalBroker globalBroker = new GlobalBroker("GlobalBroker");

			// Second step: Create Datacenters
			datacenters = new ArrayList<StorageDatacenter>(); // 不知先建数据中心代理，再建数据中心会不会有影响，以观后效
			// Third step: Create Broker
			CorrespondingStorageDatacenterBroker broker = createCSDBroker("CSdBroker");
			int brokerId = broker.getId();
			int ratesLen = 5;

			for (int i = 0; i < numDC; i++) {
				// Second step: Create Datacenters
				initNodesProperty(numNode[i], rates[i], ratesLen);

				StorageDatacenter datacenter = createNumStorageDatacenter(
						"Datacenter_" + i, numNode[i], rates[i], ratesLen,
						time_zones[i]);
				datacenters.add(datacenter);

				// Fourth step: Create VMs and Cloudlets and send them to broker
				vmList = createVM(brokerId, numNode[i], 0); // creating
															// numNode[i] vms
				broker.submitVmList(vmList);
			}
			/**
			 * 将数据中心代理与数据中心建立绑定
			 */
			broker.setDatacenters(datacenters);

			// Fifth step:create the cloudlet of reading file
			// cloudlet parameter
			long fileSize = 300;
			long outputSize = 300;
			int pesNumber = 1;
			long length = 250;// 也不知这里的length(即MI)设置合不合理
			UtilizationModel utilizationModel = new UtilizationModelFull();
			int replicaId = 0, cloudletId = 0;
			cloudletList = new ArrayList<Cloudlet>();
			replicaList = new ArrayList<BlockReplica>();
			for (int i = 0; i < numCloudFile; i++) {
				for (int j = 0; j < numReplica; j++) {
					Cloudlet cloudlet = new Cloudlet(cloudletId++, length,
							pesNumber, fileSize, outputSize, utilizationModel,
							utilizationModel, utilizationModel);
					cloudlet.setUserId(brokerId);

					BlockReplica replica = new BlockReplica(replicaId++,
							globalBroker.getId(), "CloudFile" + i + "-blk1"
									+ "-rep" + j);
					replica.init();

					if (false == cloudlet.addRequiredFile(replica.getName())) {
						Log.printLine("Data " + replica.getName()
								+ " has aready submitted to cloudlet #"
								+ cloudlet.getCloudletId());
					}
					replicaList.add(replica);
					cloudletList.add(cloudlet);
				}
			}

			broker.submitVmList(vmList);
			/**configure network before we begin to add file to the Storage Cloud*/
//			NetworkTopology.buildNetworkTopology("examples/org/cloudbus/cloudsim/examples/network/topology.brite");
			//maps CloudSim entities to BRITE entities
			//PowerDatacenter datacenter_i will correspond to BRITE node i
			double[] accrossLatency = {144.0,89.0,12.0,90.0,123.0};
//			int briteNode = 0;
			for(int i=0;i<numDC;i++){
				int dcId = datacenters.get(i).getId();
//				NetworkTopology.mapNode(dcId, briteNode);
				//addLink between broker and datacenter
				NetworkTopology.addLink(dcId,brokerId,10.0,accrossLatency[i]);
//				briteNode += 2;
			}
			
			
			broker.submitReplicaList(replicaList);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			newList.addAll(globalBroker.getBroker().getCloudletReceivedList());

			CloudSim.stopSimulation();

			printCloudletList(newList);

			// Print the debt of each user to each datacenter
			for (int i = 0; i < datacenters.size(); i++) {
				datacenters.get(i).printStorageInfo();
				datacenters.get(i).printDebts();
			}
			Log.printLine("MultiDcStorage2 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * init the storage node's averSeekTime(normally
	 * 3-15ms),transferRate(IDE/ATA 130MB/s,SATA 2 300MB/s) rotation
	 * latency(5400rpm,7200rpm,10000rpm,15000rpm),throughput
	 * 
	 * @param nodeNum
	 *            the number of hosts rates[] the different configuration
	 *            configure rate of each kind of configuration numRate the array
	 *            length of rates[]
	 */
	public static void initStorageNode(int nodeNum, double[] rates, int numRate) {
		avSeekTime = new double[nodeNum];
		transferRate = new double[nodeNum];
		rotationLatency = new double[nodeNum];
		throughPut = new double[nodeNum];
		if (numRate < 5) {
			Log.printLine("the numRate parameter is illegal in calling initNodesProperty");
			return;
		}
		double avSeekTime_0 = 0.003; // 3ms
		double avSeekTime_1 = 0.006;
		double avSeekTime_2 = 0.009;
		double avSeekTime_3 = 0.012;
		double avSeekTime_4 = 0.015;

		double throughPut_0 = 130;
		double throughPut_1 = 300;
		double throughPut_2 = 300;
		double throughPut_3 = 130;
		double throughPut_4 = 300;

		double rotationLatency_0 = 60 * 1000 / 5400 / 2.0;
		double rotationLatency_1 = 60 * 1000.0 / 7200 / 2;
		double rotationLatency_2 = 60 * 1000.0 / 10000 / 2;
		double rotationLatency_3 = 60 * 1000.0 / 15000 / 2;
		double rotationLatency_4 = 60 * 1000.0 / 15000 / 2;
		// the 1st configuration
		int start = 0;
		int end = (int) Math.floor(nodeNum * rates[0]);
		for (int i = start; i < end; i++) {
			avSeekTime[i] = avSeekTime_0;
			throughPut[i] = transferRate[i] = throughPut_0;
			rotationLatency[i] = rotationLatency_0;
		}

		// the 2nd configuration
		start = end;
		end = (int) Math.floor(nodeNum * (rates[0]+rates[1]));
		for (int i = start; i <= end; i++) {
			avSeekTime[i] = avSeekTime_1;
			throughPut[i] = transferRate[i] = throughPut_1;
			rotationLatency[i] = rotationLatency_1;
		}

		// the 3rd configuration
		start = end + 1;
		end = (int) Math.floor(nodeNum * (rates[0]+rates[1]+rates[2]));
		for (int i = start; i <= end; i++) {
			avSeekTime[i] = avSeekTime_2;
			throughPut[i] = transferRate[i] = throughPut_2;
			rotationLatency[i] = rotationLatency_2;
		}
		// the 4th configuration
		start = end + 1;
		end = (int) Math.floor(nodeNum * (rates[0]+rates[1]+rates[2]+rates[3]));
		for (int i = start; i <= end; i++) {
			avSeekTime[i] = avSeekTime_3;
			throughPut[i] = transferRate[i] = throughPut_3;
			rotationLatency[i] = rotationLatency_3;
		}

		// the 5th configuration
		start = end + 1;
		end = (int) Math.floor(nodeNum * (rates[0]+rates[1]+rates[2]+rates[3]+rates[4]));
		for (int i = start; i < nodeNum; i++) {
			avSeekTime[i] = avSeekTime_4;
			throughPut[i] = transferRate[i] = throughPut_4;
			rotationLatency[i] = rotationLatency_4;
		}
	}

	// 下面是创建指定数目主机、指定配置 比例与时区的的数据中心的步骤
	private static StorageDatacenter createNumStorageDatacenter(String name,
			int numHost, double[] rates, int ratsLen, double timeZone)
			throws ParameterException {
		// 1、创建主机列表
		List<Host> hostList = new ArrayList<Host>();
		// PE及其主要参数
		int hostId = 0;

		// storage node property,actually storage node is host,but with
		// compatibility to CloudSim,here we instantiate them individually
		double networkLatency = 0.0025;// in second,the internal network latency
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		initStorageNode(numHost, rates, ratsLen);

		for (int i = 0; i < numHost; i++) {
			// 2、创建PE列表
			List<Pe> peList = new ArrayList<Pe>();
			// 3、创建PE并加入列表
			peList.add(new Pe(0, new PeProvisionerSimple(mipsLst[i])));
			// 4、创建主机并加入列表
			hostList.add(new Host(hostId, new RamProvisionerSimple(ramLst[i]),
					new BwProvisionerSimple(bw), (long) storageCapLst[i],
					peList, new VmSchedulerSpaceShared(peList)));

			storageList.add(new StorageNode(i, new RamProvisionerSimple(
					ramLst[i]), new BwProvisionerSimple(bw),
					(long) storageCapLst[i], peList, bw, networkLatency,
					avSeekTime[i], transferRate[i], rotationLatency[i],
					throughPut[i]));
			hostId++;
		}

		// 数据中心特征参数
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		// double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;

		// 5、创建数据中心特征对象
		@SuppressWarnings("unchecked")
		StorageDatacenterCharacteristics characteristics = new StorageDatacenterCharacteristics(
				arch, os, vmm, hostList,
				(List<? extends StorageNode>) storageList, timeZone, cost,
				costPerMem, costPerStorage, costPerBw);

		// 6、创建数据中心对象
		StorageDatacenter datacenter = null;
		try {
			datacenter = new StorageDatacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
			for (int i = 0; i < numHost; i++) {
				((StorageNode) storageList.get(i)).setSdDatacenter(datacenter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	// 创建数据中心代理
	private static CorrespondingStorageDatacenterBroker createCSDBroker(
			String name) {
		CorrespondingStorageDatacenterBroker broker = null;
		try {
			broker = new CorrespondingStorageDatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}

	// 静态内部类
	public static class GlobalBroker extends SimEntity {

		private static final int CREATE_BROKER = 0;
		private List<Vm> vmList;
		private List<Cloudlet> cloudletList;
		private StorageDatacenterBroker broker;

		public GlobalBroker(String name) {
			super(name);
			vmList = new ArrayList<Vm>();
			cloudletList = new ArrayList<Cloudlet>();
		}

		@Override
		public void processEvent(SimEvent ev) {
			switch (ev.getTag()) {
			case CREATE_BROKER:
				setBroker(createCSDBroker("globalBroker"));
				getBroker().setDatacenters(datacenters);
	
				vmID = 0;
				int ratesLen = 5;
				//take care that every time to create vm or host or node,the class member such 
				//as the ramLst ... is different ,so don't forget to initNodeProperty every ..
				for (int i = 0; i < numDC; i++) {
					initNodesProperty(numNode[i], rates[i], ratesLen);
					setVmList(createVM(getBroker().getId(), numNode[i], 0));
				}
				//suppose add replica random
				List<BlockReplica> repList = new ArrayList<BlockReplica>();
				int repSize = replicaList.size();
				int numN = 10;
				long fileSize = 300;
				long outputSize = 300;
				int pesNumber = 1;
				long length = 250;// 也不知这里的length(即MI)设置合不合理
				UtilizationModel utilizationModel = new UtilizationModelFull();
				int cloudletId = 0;
				for (int i = 0; i < numN; i++) {
					int tmp =ThreadLocalRandom.current().nextInt(repSize);
					repList.add(replicaList.get(tmp));
					Cloudlet cloudlet = new Cloudlet(100 + cloudletId, length,
							pesNumber, fileSize, outputSize, utilizationModel,
							utilizationModel, utilizationModel);
					cloudletId ++;
					cloudlet.setUserId(getBroker().getId());
					cloudlet.addRequiredFile(replicaList.get(tmp).getName());
					List<Cloudlet> cloudlst = new ArrayList<Cloudlet>();
					cloudlst.add(cloudlet);
					setCloudletList(cloudlst);
				}
				
				getBroker().submitReplicaList(repList);
				broker.submitVmList(getVmList());
				broker.submitCloudletList(getCloudletList());

				CloudSim.resumeSimulation();

				break;

			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}

		@Override
		public void startEntity() {
			Log.printLine("GlobalBroker is starting...");
			schedule(getId(), 200, CREATE_BROKER);
		}

		@Override
		public void shutdownEntity() {
		}

		public List<Vm> getVmList() {
			return vmList;
		}

		protected void setVmList(List<Vm> vmList) {
			this.vmList.addAll(vmList);
		}

		public List<Cloudlet> getCloudletList() {
			return cloudletList;
		}

		protected void setCloudletList(List<Cloudlet> cloudletList) {
			this.cloudletList.addAll(cloudletList);
		}

		public StorageDatacenterBroker getBroker() {
			return broker;
		}

		protected void setBroker(StorageDatacenterBroker broker) {
			this.broker = broker;
		}

	}

}
