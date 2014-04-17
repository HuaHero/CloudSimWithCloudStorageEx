/**
 * Title:SingleDcTOPSISstorage.java
 * Description:在CloudSim中，将任务抽象为要多大空间、多少CPU指令等，然后分配给VM即是资源调度、分配的问题；
 * 				那么，在这里，是为仿真云存储中副本放置的问题，副本 Block_Replica与VM一样，最终是绑到哪个DataCenter、哪个
 * 				主机等,因此，在云存储中，这里模仿VM来构造Block_Replica,将CloudLet扩展为两个读ReadCloudLet和
 * 				写WriteCloudLet，这里面，对副本分配到Host，即是一种副本管理策略,本程序的放置策略是TOPSIS
 * 
 * 				在本工程中，为模拟HDFS块存储方式，同时利用尽量利用原有工作，加入的类有BlockReplica、Block、CloudFile，
 * 		        	其中，上述从左至右为继承关系，即靠左的类继承靠右的类，CloudFile继承CloudSim中的File类，从右为左为包含关系 
 * 注意：在对CloudSim-2-1-1进行模拟云存储扩展时，本着尽量不改动原框架代码的基础上加入自己的东西；
 * 	         但是，原框架中，在CloudSim.starSimulation()深层调用中,在执行完跳出时其实已注销了所有SimEntity,在云存储中
	        我们需要将这注销这些SimEntity的事情只放在stopSimulation()中，因此，我将CloudSim.finishSimulation()
	       和CloudSim.runStop()放在CloudSim.stopSimulation()函数中前部；
	       还有，cloudlet.addRequiredFile()中原框架中加入成功了文件名后，并没有将操作结果正确布尔值置为True
 *detail:浪潮公司的《云数据中心操作系统副本分布算法的设计与实现》采用TOPSIS副本分布算法，其中选取的评价指标为：
 *		（1）正向指标：存储节点的存储空间、IOPS、Throughput
 *		（2）负向指标：存储节点的空间使用率、CPU使用率、内存使用率
 *		其采用CloudSim模拟三种事件：文件存储、节点加入、节点离开
 *      其假定节点在时间T内的故障概率为1-exp(-T/langbada),langbada越大，节点更高靠，实难中langbada取等于5年
 *      其假定文件存储时间满足泊松分布，即单位时间（1个小时）产生k个文件存储事件的概率为power(u,k)*exp(-u)/k,u取1000
 *      节点加入事件与文件存储事件满足同样的概率分布，取每月平均加入1个新节点
 *      初始设100个节点，副本冗余度为3，每次写入大小为5G的文件
 * Date:2014/2/13
 * @author :HuaHero(huahero.long@gmail.com)
 * license GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2013-2014 Xiamen University,China
 */
package cloudStorage;

/*
 import java.text.DecimalFormat;
 import java.util.*;
 import org.cloudbus.cloudsim.core.*;
 */
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

//import org.cloudbus.cloudsim.Vm;
//对照Vm，这里我要创建副本类

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * 为避免对已有工作的影响，我将对既有可用的类采取继承方式新建以YunHai开头的类,例如，为支持对节点的存储空间等属性
 * 的评估和权重，引入权重类YunHaiWeight
 * @author huahero
 *
 */
public class SingleDcTOPSISstorage2 {
	private static List<Cloudlet> cloudletList; // 任务列表
	private static List<Vm> vmList; // 虚拟机列表
	// private static int cloudletNum = 10; // 任务总数,也反应了用户数

	private static List<BlockReplica> replicaList; // 副本列表
	private static int replicaNum = 4; //副本冗余度为3
	private static int cloudFileNum = 5; //for test

	private static int nodeNum = 10;//10 for test 
	// private static long replicaTotal= replicaNum * cloudFileNum;
	/** nodes' properties */
	// private static int mips = 1000;
	private static int[] mipsLst;
	// private static int ram = 2048;
	private static int[] ramLst;
	// private static long storage = 1000000;//1024*1024; //1T
	private static double[] storageCapLst;
	private static int bw = 10000;

	private static double[] avSeekTime;
	private static double[] transferRate;
	private static double[] rotationLatency;
	private static double[] throughPut;
	
	// private static final double blockSize = 64;//默认给文件按64MB大小分块
	/**
	 * init the nodes'mips,ram,storageCapacity property
	 * 
	 * @param nodeNum
	 *            the number of nodes
	 */
	public static void initNodesProperty(int nodeNum) {
		mipsLst = new int[nodeNum];
		ramLst = new int[nodeNum];
		storageCapLst = new double[nodeNum];
		mipsLst[0] = 1250;
		mipsLst[1] = 2000;
		mipsLst[2] = 1500;
		mipsLst[3] = 2500;
		mipsLst[4] = 2250;
		ramLst[0] = 512;
		ramLst[1] = 512;
		ramLst[2] = 1024;
		ramLst[3] = 2048;
		ramLst[4] = 4096;
		storageCapLst[0] = 100000.0;//1000000.0;
		storageCapLst[1] = 250000.0;//1005000.0;
		storageCapLst[2] = 500000.0;
		storageCapLst[3] = 750000.0;//2000000.0;
		storageCapLst[4] = 1000000.0;//4000000.0;
		for (int i = 5; i < nodeNum; i++) {
			mipsLst[i] = 1000;
			ramLst[i] = 1024;
			storageCapLst[i] = 500000.0;//1000000.0;
		}
	}

	/**
	 * Create Vm with given userId and number of Vms
	 * 
	 * @param userId
	 *            the userId
	 * @param vmNum
	 *            the number of Vms
	 * @return the created list of Vms
	 */
	public static List<Vm> createVM(int userId, int vmNum) {
		/**
		 * the container which to store the Vms,this list is passed to broker
		 * later...
		 */
		List<Vm> vmLst = new ArrayList<Vm>();

		for (int i = 0; i < vmNum; i++) {
			// Vm parameter
			double size = storageCapLst[i]; // the image size of vm
			long bw = 1000; // the bandwidth
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name
			vmLst.add(new Vm(i, userId, mipsLst[i], pesNumber, ramLst[i], bw,
					(long) size, vmm, new CloudletSchedulerTimeShared()));
		}
		return vmLst;
	}

	// public static List<Cloudlet> createCloudlet(int userId,int cloudLetNum);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting SingleDcTOPSISstorage2...");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			// 云计算仿真的必然步骤
			// 第一步：初始化CloudSim包
			CloudSim.init(num_user, calendar, trace_flag);

			initNodesProperty(nodeNum);
			// 第二步：创建数据中心
			// Datacenter datacenter0 = createDatacenter("Datacenter_0");

			StorageDatacenter datacenter0 = createNumStorageDatacenter(
					"Datacenter_0", nodeNum);

			// 第三步：创建数据中心代理
			// DatacenterBroker broker = createBroker();
			StorageDatacenterBroker broker = createSDBroker();
			int brokerId = broker.getId();

			/**
			 * 将数据中心代理与数据中心建立绑定
			 */
			List<StorageDatacenter> datacenterList = new ArrayList<StorageDatacenter>();
			datacenterList.add(datacenter0);
			broker.setDatacenters(datacenterList);

			// 第四步：创建虚拟机
			vmList = createVM(brokerId, nodeNum);
			broker.submitVmList(vmList);

			// 任务参数
			// tast size
			long fileSize = 300;
			long outputSize = 300;
			int pesNumber = 1;
			long length = 250;// 也不知这里的length(即MI)设置合不合理
			UtilizationModel utilizationModel = new UtilizationModelFull();
			// 第五步：创建云任务

			/*
			 * 第六步：创建数据副本,建议文件名中不用连字符，块名用“所属文件名-blk加排号”表示，而副本则用“所属文件名-blk加排号-rep加排号
			 * ”表示 6.1 创建文件CloudFile 6.2 给文件CloudFile分块Block 6.3
			 * 每个Block创建副本BlockReplica,利用org.cloudbus.cloudsim.File
			 * 实际上，按实际,Cloudlet Num 应该小于或等于读写数据块数
			 */
			cloudletList = new ArrayList<Cloudlet>();
			replicaList = new ArrayList<BlockReplica>();

			int replicaId = 1, cloudletId = 0;
			for (int i = 1; i <= cloudFileNum; i++) {
				for (int j = 1; j <= replicaNum; j++) {
					Cloudlet cloudlet1 = new Cloudlet(cloudletId++, length,
							pesNumber, fileSize, outputSize, utilizationModel,
							utilizationModel, utilizationModel);
					cloudlet1.setUserId(brokerId);

					BlockReplica replica = new BlockReplica(replicaId++,
							brokerId, "CloudFile" + i + "-blk1" + "-rep" + j/*+ j*/); //使同一个数据的副本名相同，只是存储位置不同而已
					replica.init();

					if (false == cloudlet1.addRequiredFile(replica.getName())) {
						Log.printLine("Data " + replica.getName()
								+ " has aready submitted to cloudlet #"
								+ cloudlet1.getCloudletId());
					}

					replicaList.add(replica);
					cloudletList.add(cloudlet1);
				}

			}
			// 提交数据副本s
			broker.submitReplicaList(replicaList);
			/** 创建副本，应该要触 发CloudSim.VM_ADD_DATA，这个到时再看是怎么去实现 */

			// submit cloudlet list to the broker // 提交任务列表
			broker.submitCloudletList(cloudletList);
			// 第六步：绑定任务到虚拟机
			/** 在我们的云存储模拟中，将读或写任务绑定到虚拟机，里面涉及到对数据的布局、创建等，且我们的任务对等数据（大小），虚拟机对应存储节点 */


			// 第七步：启动仿真
			CloudSim.startSimulation();
			// 在源码中，CloudSim.startSimulation()在执行完跳出时其实已注销了SimEntity,在云存储中
			// 我们需要将这注销这些SimEntity的事情只放在stopSimulation()中

			// 第八步：统计结果并输出结果
			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();
			printCloudletList(newList);
			datacenter0.printStorageInfo();
			datacenter0.printDebts();
			Log.printLine("SingleDcTOPSISstorage2 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happened!");
		}
	}

	/**
	 * init the storage node's averSeekTime(normally 3-15ms),transferRate(IDE/ATA 130MB/s,SATA 2 300MB/s)
	 * rotation latency(5400rpm,7200rpm,10000rpm,15000rpm),throughput
	 * @param nodeNum
	 */
	public static void initStorageNode(int nodeNum){
		avSeekTime = new double[nodeNum];
		 transferRate = new double[nodeNum];
		 rotationLatency = new double[nodeNum];
		 throughPut = new double[nodeNum];
		 avSeekTime[0] = 0.011; //6ms
		 avSeekTime[1] = 0.009;
		 avSeekTime[2] = 0.008;
		 avSeekTime[3] = 0.006;
		 avSeekTime[4] = 0.006;
		 
		 throughPut[0] = transferRate[0] =130;
		 throughPut[1] = transferRate[1] = 130;
		 throughPut[2] = transferRate[2] = 300;
		 throughPut[3] = transferRate[3] =130;
		 throughPut[4] = transferRate[4] =300;
		 
		 rotationLatency[0] = 60*1000/5400/2.0;
		 rotationLatency[1] = 60 * 1000.0/7200/2;
		 rotationLatency[2] = 60 * 1000.0/10000/2;
		 rotationLatency[3] = 60 *1000.0/15000/2;
		 rotationLatency[4] = 60 * 1000.0/15000/2;
		 for(int i=5;i<nodeNum;i++){
			 avSeekTime[i] = 0.009;
			 throughPut[i] = transferRate[i] = 130;
			 rotationLatency[i] = 60 * 1000.0/7200/2;
		 }
	}
	
	// 下面是创建指定数目主机的数据中心的步骤
	private static StorageDatacenter createNumStorageDatacenter(String name,
			int numHost) throws ParameterException {
		// 1、创建主机列表
		List<Host> hostList = new ArrayList<Host>();
		// PE及其主要参数
		int hostId = 0;

		// storage node property,actually storage node is host,but with
		// compatibility to CloudSim,here we instantiate them individually
		double networkLatency = 0.0025;// second
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		initStorageNode(numHost);
		
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
					(long) storageCapLst[i], peList,
					bw, networkLatency,
					avSeekTime[i], transferRate[i], rotationLatency[i],
					throughPut[i]));
			hostId++;
		}

		// 数据中心特征参数
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;

		// 5、创建数据中心特征对象
		@SuppressWarnings("unchecked")
		StorageDatacenterCharacteristics characteristics = new StorageDatacenterCharacteristics(
				arch, os, vmm, hostList,
				(List<? extends StorageNode>) storageList, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		// 6、创建数据中心对象
		StorageDatacenter datacenter = null;
		try {
			datacenter = new StorageDatacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

	// 创建数据中心代理
	private static StorageDatacenterBroker createSDBroker() {
		StorageDatacenterBroker broker = null;
		try {
			broker = new StorageDatacenterBroker("SdBroker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	// 输出统计信息
	private static void printCloudletList(List<Cloudlet> cloudletlist) {
		int size = cloudletlist.size();
		Cloudlet cloudlet;
		String indent = "	";
		Log.printLine();
		Log.printLine("==========OUTPUT==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = cloudletlist.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}
