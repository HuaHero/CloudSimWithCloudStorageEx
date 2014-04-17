package cloudStorage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * 
 * @author HuaHero(huahero.long@gmail.com)
 * ���Բ������������ͬ������CloudSim��������ʵ��ģ���ƴ洢����CloudSim��������������ƣ����ʵ��Ķ�Ӧ�ÿ��ԣ������Ƿ����
 * (1)2014/4/14 13��27 ����CloudSim���ṩ�Ŀ�ܼ�API��Cloudlet��addFile()��������û��Vm���У���Ϊ��дCloudLet��Ҫ������Vm��
 * ��2�����������Լ�ʵ�ֶ��ڶ�дֱ���ɻ����ʹ������ 
 * des:ʵ���ƴ洢ϵͳģ�⣬���CloudSim����ʾ����ʵ���ɴ����������ݵĶ���д���ֲ�����ʵ��������ʱ��̬�������ݡ�ɾ��������������Ǩ��
 */

public class CloudStorageImprove {
	/** The data list */
	private static List<BlockReplica> replicaList;
//	public static List<BlockReplica> replicaList;

	/** The federation clouds's scale,number of datacenter */
	private static short numDC = 1;			//for starting from a simple test
	private static List<StorageDatacenter> datacenters;

	/** The block replica / data number */
	private static int numCloudFile = 15;
	private static int numReplica = 3;

	/** the number hosts/storage nodes of each datacenter */
	 private static int[] numNode = {20,15,30,40,55};
	// private static int[] numNode = { 2, 1, 3, 4, 5 };

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
	 * init the nodes'mips,ram,storageCapacity property,given the different
	 * configure
	 * 
	 * @param nodeNum  the number of nodes
	 * @param rate the rate of one configuration occurs,the sum of the rate must equal to 1.0
	 * @param numRate the size of the rate array,actually in this program ,I thought there are 5 kinds of configuration default
	 *            
	 *            
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

	// //////////////////////// STATIC METHODS ///////////////////////
	/**
	 * 
	 * @param brokerId the id of broker
	 * @param numCloudFile the number of file
	 * @param numCopy the number of replicas
	 * @throws ParameterException 
	 */
	private static List<BlockReplica> createReplicas(int brokerId,int numCloudFile,int numCopy,int idShift) throws ParameterException{
		List<BlockReplica> repList = new ArrayList<BlockReplica>();
		int replicaId = 0;
		for (int i = 0; i < numCloudFile; i++) {
			for (int j = 0; j < numCopy; j++) {
				BlockReplica replica = new BlockReplica(idShift+replicaId++,
						brokerId, "CloudFile" + i + "-blk1"
								+ "-rep" + j);
				replica.init();

				/*if (false == cloudlet.addRequiredFile(replica.getName())) {
					Log.printLine("Data " + replica.getName()
							+ " has aready submitted to cloudlet #"
							+ cloudlet.getCloudletId());
				}*/
				repList.add(replica);
			}
		}
		return repList;
	}

	/**
	 * Creates main() to run this example
	 * 
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream oldPrintStream = System.out;
		FileOutputStream bos = new FileOutputStream("CloudStorageImprove .txt");
		MultiOutputStream multi = new MultiOutputStream(new PrintStream(bos),
				oldPrintStream);
		System.setOut(new PrintStream(multi));
		Log.printLine("Starting CloudStorageImprove ...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = numDC; // number of grid users,here I just set it
									// equal to datacentr number
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			datacenters = new ArrayList<StorageDatacenter>(); // ��֪�Ƚ��������Ĵ������ٽ��������Ļ᲻����Ӱ�죬�Թۺ�Ч
			// Third step: Create Broker
			StorageDatacenterBrokerImprov broker = createSDBroker("SdBroker");
			int brokerId = broker.getId();
			int ratesLen = 5;

			for (int i = 0; i < numDC; i++) {
				// Second step: Create Datacenters
				initNodesProperty(numNode[i], rates[i], ratesLen);

				StorageDatacenter datacenter = createNumStorageDatacenter(
						"Datacenter_" + i, numNode[i], rates[i], ratesLen,
						time_zones[i]);
				datacenters.add(datacenter);
			}
			/**
			 * ���������Ĵ������������Ľ�����
			 */
			broker.setDatacenters(datacenters);
			
			//ȥ��Vm��ȥ��Cloudlet
			
			//Third step:create data item,which will be scheduled by broker
//			replicaList = new ArrayList<BlockReplica>();
			replicaList = createReplicas(broker.getId(), numCloudFile, numReplica, 0);

			/**configure network before we begin to add file to the Storage Cloud*/
//			NetworkTopology.buildNetworkTopology("examples/org/cloudbus/cloudsim/examples/network/topology.brite");
			//maps CloudSim entities to BRITE entities
			//PowerDatacenter datacenter_i will correspond to BRITE node i
			//I have placed these codes in the broker's setDatacenters
			double[] accrossLatency = {144.0,89.0,12.0,90.0,123.0};
			for(int i=0;i<numDC;i++){
				int dcId = datacenters.get(i).getId();
				//addLink between broker and datacenter
				NetworkTopology.addLink(dcId,brokerId,10.0,accrossLatency[i]);
			}
			
			AddedBroker addedBroker = new AddedBroker("AddedBroker");
			broker.submitReplicaList(replicaList);
			broker.bindReplicasToStorageNodeRand();
			
			// Print the debt of each user to each datacenter
			for (int i = 0; i < datacenters.size(); i++) {
				datacenters.get(i).printStorageInfo();
				datacenters.get(i).printDebts();
			}
			//a thread that will create new broker at 20 clock time
			//learnt from CloudSimExample7.java
			Runnable monitor = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					CloudSim.pauseSimulation(20);
					while(true){
						if(CloudSim.isPaused()){
							break;
						}
						try{
							Thread.sleep(1000);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
					
					Log.printLine("\n\n\n" + CloudSim.clock() + ": The simulation is paused for 5 sec \n\n");
					try{
						Thread.sleep(5000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					
					StorageDatacenterBrokerImprov SDBroker = createSDBroker("SDBroker2");
					int sdBrokerId = SDBroker.getId();
					try{
						List<BlockReplica> repList = createReplicas(sdBrokerId,10,5,numCloudFile*numReplica+10);
						SDBroker.setDatacenters(datacenters);
						SDBroker.submitReplicaList(repList);
						SDBroker.bindReplicasToStorageNodesTOPSIS();
					}catch(ParameterException e){
						e.printStackTrace();
					}
					
					CloudSim.resumeSimulation();
				}
				
			};
			new Thread(monitor).start();
			//for this case,the CloudSim.startSimulation() just take action to work for registering the resources
			CloudSim.startSimulation();		
			
			//CloudSim.pauseSimulation(200);//why this is not really work
			
			CloudSim.stopSimulation();


			// Print the debt of each user to each datacenter
			for (int i = 0; i < datacenters.size(); i++) {
				datacenters.get(i).printStorageInfo();
				datacenters.get(i).printDebts();
			}
			Log.printLine("StroageTest finished!");
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

	// �����Ǵ���ָ����Ŀ������ָ������ ������ʱ���ĵ��������ĵĲ���
	private static StorageDatacenter createNumStorageDatacenter(String name,
			int numHost, double[] rates, int ratsLen, double timeZone)
			throws ParameterException {
		// 1�����������б�
		List<Host> hostList = new ArrayList<Host>();
		// PE������Ҫ����
		int hostId = 0;

		// storage node property,actually storage node is host,but with
		// compatibility to CloudSim,here we instantiate them individually
		double networkLatency = 0.0025;// in second,the internal network latency
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		initStorageNode(numHost, rates, ratsLen);

		for (int i = 0; i < numHost; i++) {
			// 2������PE�б�
			List<Pe> peList = new ArrayList<Pe>();
			// 3������PE�������б�
			peList.add(new Pe(0, new PeProvisionerSimple(mipsLst[i])));
			// 4�����������������б�
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

		// ����������������
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		// double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;

		// 5����������������������
		@SuppressWarnings("unchecked")
		StorageDatacenterCharacteristics characteristics = new StorageDatacenterCharacteristics(
				arch, os, vmm, hostList,
				(List<? extends StorageNode>) storageList, timeZone, cost,
				costPerMem, costPerStorage, costPerBw);

		// 6�������������Ķ���
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
	// �����������Ĵ���
	private static StorageDatacenterBrokerImprov createSDBroker(String name) {
		StorageDatacenterBrokerImprov broker = null;
		try {
			broker = new StorageDatacenterBrokerImprov(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	///////////////static inner Class//////////////////
	static class AddedBroker extends StorageDatacenterBrokerImprov{
		//internal event
		private static final int CREATE_BROKER = 47;

		public AddedBroker(String name) throws Exception {
			super(name);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void processEvent(SimEvent ev) {
			// TODO Auto-generated method stub
			switch(ev.getTag()){
			case CREATE_BROKER:
				//
				this.setDatacenters(CloudStorageImprove.datacenters);
				CloudSim.resumeSimulation();
				//write or read data
//				this.submitReplicaList(replicaList);
				Operate();
				break;
			default:
				break;
			}
		}

		@Override
		public void shutdownEntity() {
			// TODO Auto-generated method stub
			super.shutdownEntity();
		}

		@Override
		public void startEntity() {
			// TODO Auto-generated method stub
			Log.printLine("AddedBroker is starting ...");
			schedule(getId(), 100, CREATE_BROKER);
		}
		
		public void Operate(){
			//generate read operation randomly
			Log.printLine("Operate ...");
			int numCopy = CloudStorageImprove.replicaList.size();
			int numRead = 5;
			for(int i=0;i<numCopy;i++){
				int selIndex = ThreadLocalRandom.current().nextInt(numRead);
				readData(CloudStorageImprove.replicaList.get(selIndex));
			}
		}
	}

}