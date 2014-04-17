/**
 * Title:SingleDcTOPSISstorage.java
 * Description:��CloudSim�У����������ΪҪ���ռ䡢����CPUָ���ʵ�壬Ȼ������VM������Դ���ȡ���������⣻
 * 				��ô���������Ϊ�����ƴ洢�и������õ����⣬���� Block_Replica��VMһ���������ǰ��ĸ�DataCenter���ĸ�
 * 				������,��ˣ����ƴ洢�У�����ģ��VM������Block_Replica,��CloudLet��չΪ������ReadCloudLet��
 * 				дWriteCloudLet�������棬�Ը������䵽Host������һ�ָ����������,������ķ��ò�����TOPSIS
 * 
 * 				�ڱ������У�Ϊģ��HDFS��洢��ʽ��ͬʱ���þ�������ԭ�й��������������BlockReplica��Block��CloudFile��
 * 		        	���У�������������Ϊ�̳й�ϵ�����������̳п��ҵ��࣬CloudFile�̳�CloudSim�е�File�࣬����Ϊ��Ϊ������ϵ 
 * ע�⣺�ڶ�CloudSim-2-1-1����ģ���ƴ洢��չʱ�����ž������Ķ�ԭ��ܴ���Ļ����ϼ����Լ��Ķ�����
 * 	         ���ǣ�ԭ����У���CloudSim.starSimulation()��������,��ִ��������ʱ��ʵ��ע��������SimEntity,���ƴ洢��
	        ������Ҫ����ע����ЩSimEntity������ֻ����stopSimulation()�У���ˣ��ҽ�CloudSim.finishSimulation()
	       ��CloudSim.runStop()����CloudSim.stopSimulation()������ǰ����
	       ���У�cloudlet.addRequiredFile()��ԭ����м���ɹ����ļ����󣬲�û�н����������ȷ����ֵ��ΪTrue
	       
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
//����Vm��������Ҫ����������

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class SingleDcTOPSISstorage {
	private static List<Cloudlet> cloudletList; // �����б�
	private static List<Vm> vmList; // ������б�
	// private static int cloudletNum = 10; // ��������,Ҳ��Ӧ���û���

	private static List<BlockReplica> replicaList; // �����б�
	private static int replicaNum = 10; // �ɶ�����ʱָ���ļ�CloudFile���ڲ�����
	private static int cloudFileNum = 10;

	/**100���ڵ�*/
	private static int nodeNum = 100;
	// private static long replicaTotal= replicaNum * cloudFileNum;
	/** nodes' properties */
	// private static int mips = 1000;
	private static int[] mipsLst;
	// private static int ram = 2048;
	private static int[] ramLst;
	// private static long storage = 1000000;//1024*1024; //1T
	private static double[] storageCapLst;
	private static int bw = 10000;
	
	

	// private static final double blockSize = 64;//Ĭ�ϸ��ļ���64MB��С�ֿ�
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
		storageCapLst[0] = 1000000.0;
		storageCapLst[1] = 1005000.0;
		storageCapLst[2] = 512000.0;
		storageCapLst[3] = 2000000.0;
		storageCapLst[4] = 4000000.0;
		for (int i = 5; i < nodeNum; i++) {
			mipsLst[i] = 1000;
			ramLst[i] = 4096;//1024;		//4GRAM
			storageCapLst[i] = 5000000.0; //5T
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
		Log.printLine("Starting SingleDcTOPSISstorage...");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			// �Ƽ������ı�Ȼ����
			// ��һ������ʼ��CloudSim��
			CloudSim.init(num_user, calendar, trace_flag);

			initNodesProperty(nodeNum);
			// �ڶ�����������������
			// Datacenter datacenter0 = createDatacenter("Datacenter_0");

			StorageDatacenter datacenter0 = createNumStorageDatacenter(
					"Datacenter_0", nodeNum);

			// �������������������Ĵ���
			// DatacenterBroker broker = createBroker();
			StorageDatacenterBroker broker = createSDBroker();
			int brokerId = broker.getId();

			/**
			 * ���������Ĵ������������Ľ�����
			 */
			List<StorageDatacenter> datacenterList = new ArrayList<StorageDatacenter>();
			datacenterList.add(datacenter0);
			broker.setDatacenters(datacenterList);

			// ���Ĳ������������
			vmList = createVM(brokerId, nodeNum);
			broker.submitVmList(vmList);

			// �������
			// tast size
			long fileSize = 300;
			long outputSize = 300;
			int pesNumber = 1;
			long length = 250;// Ҳ��֪�����length(��MI)���úϲ�����
			UtilizationModel utilizationModel = new UtilizationModelFull();
			// ���岽������������

			/*
			 * ���������������ݸ���,�����ļ����в������ַ��������á������ļ���-blk���źš���ʾ�����������á������ļ���-blk���ź�-rep���ź�
			 * ����ʾ 6.1 �����ļ�CloudFile 6.2 ���ļ�CloudFile�ֿ�Block 6.3
			 * ÿ��Block��������BlockReplica,����org.cloudbus.cloudsim.File
			 * ʵ���ϣ���ʵ��,Cloudlet Num Ӧ��С�ڻ���ڶ�д���ݿ���
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
							brokerId, "CloudFile" + i + "-blk1" + "-rep" + j);
					replica.init();

					if (false == cloudlet1.addRequiredFile(replica.getName())) {
						Log.printLine("Data " + replica.getName()
								+ " has failed to submit to cloudlet #"
								+ cloudlet1.getCloudletId());
					}

					replicaList.add(replica);
					cloudletList.add(cloudlet1);
				}

			}
			// �ύ���ݸ���
			broker.submitReplicaList(replicaList);
			/** ����������Ӧ��Ҫ�� ��CloudSim.VM_ADD_DATA�������ʱ�ٿ�����ôȥʵ�� */

			// submit cloudlet list to the broker // �ύ�����б�
			broker.submitCloudletList(cloudletList);
			// �������������������
			/** �����ǵ��ƴ洢ģ���У�������д����󶨵�������������漰�������ݵĲ��֡������ȣ������ǵ�����Ե����ݣ���С�����������Ӧ�洢�ڵ� */
			// broker.bindReplicasToStorageNodesTOPSIS();
			// broker.bindReplicasToStorageNodesSimple();//��bindCloudletsToVms�е���

			// ���߲�����������
			CloudSim.startSimulation();
			// ��Դ���У�CloudSim.startSimulation()��ִ��������ʱ��ʵ��ע����SimEntity,���ƴ洢��
			// ������Ҫ����ע����ЩSimEntity������ֻ����stopSimulation()��

			// �ڰ˲���ͳ�ƽ����������
			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();
			printCloudletList(newList);
			datacenter0.printStorageInfo();
			datacenter0.printDebts();
			Log.printLine("SingleDcTOPSISstorage finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happened!");
		}
	}

	
	// �����Ǵ���ָ����Ŀ�������������ĵĲ���
	private static StorageDatacenter createNumStorageDatacenter(String name,
			int numHost) throws ParameterException {
		// 1�����������б�
		// List<StorageNode> nodeList = new ArrayList<StorageNode>();
		List<Host> hostList = new ArrayList<Host>();
		// PE������Ҫ����
		int hostId = 0;

		// storage node property,actually storage node is host,but with
		// compatibility to CloudSim,here we instantiate them individually
		double networkLatency = 4.17;// ms
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		// LinkedList<Storage> storaeList = nodeList;
		

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
					ramLst[i]), new BwProvisionerSimple(bw), storageCapLst[i],
					peList, bw, networkLatency));
			
			hostId++;
		}

		// ����������������
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;

		// 5����������������������
		@SuppressWarnings("unchecked")
		StorageDatacenterCharacteristics characteristics = new StorageDatacenterCharacteristics(
				arch, os, vmm, hostList,
				(List<? extends StorageNode>) storageList, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		// 6�������������Ķ���
		StorageDatacenter datacenter = null;
		try {
			/*
			 * datacenter = new StorageDatacenter(name, null, characteristics,
			 * null, new ReplicaAllocationPolicySimple(nodeList), storageList,
			 * 0);
			 */
			datacenter = new StorageDatacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

	// �����������Ĵ���
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

	// ���ͳ����Ϣ
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
