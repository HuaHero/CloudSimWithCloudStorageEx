package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudSimExtendedExample1 {

	/**
	 * @param args
	 */
	private static List<Cloudlet> cloudletList;		//任务列表
	private static int cloudletNum = 10;				//任务总数
	
	private static List<Vm> vmList;					//虚拟机列表
	private static int vmNum = 5;					//虚拟机总数
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting CloudSimExtendedExample1...");
		try{
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			//云计算仿真的必然步骤
			//第一步：初始化CloudSim包
			CloudSim.init(num_user,calendar,trace_flag);
			
			//第二步：创建数据中心
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			
			//第三步：创建数据中心代理
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			
			//设置虚拟机参数
			int vmid = 0;
			int[] mips = new int[]{278,289,132,209,286};
			long size = 10000;
			int ram = 2048;
			long bw = 1000;
			int pesNumber = 1;
			String vmm = "Xen";
			
			//第四步：创建虚拟机
			vmList = new ArrayList<Vm>();
			for(int i = 0;i < vmNum;i++){
				vmList.add(new Vm(vmid,brokerId,mips[i],pesNumber,ram,bw,size,vmm,new CloudletSchedulerSpaceShared()));
				vmid++;
			}
			//提交虚拟机列表
			broker.submitVmList(vmList);
			
			//任务参数
			int id = 0;
			long[] lengths = new long[]{19365,49809,30218,44157,16754,18336,20045,31493,30727,31017};
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			
			//第五步：创建云任务
			cloudletList = new ArrayList<Cloudlet>();
			for(int i = 0;i<cloudletNum;i++){
				Cloudlet cloudlet = new Cloudlet(id,lengths[i],pesNumber,fileSize,outputSize,
						utilizationModel,utilizationModel,utilizationModel);
				cloudlet.setUserId(brokerId);
				cloudletList.add(cloudlet);
				id++;
			}
			//提交任务列表
			broker.submitCloudletList(cloudletList);
			
			//第六步：绑定任务到虚拟机
			//broker.bindCloudletsToVmsSimple();//简单遍历哈希虚拟主机循环列表
			broker.bindCloudletsToVmsTimeAwared();
			
			
			//第七步：启动仿真
			CloudSim.startSimulation();
			
			//第八步：统计结果并输出结果
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();
			printCloudletList(newList);
			datacenter0.printDebts();
			Log.printLine("CloudSimExtendedExample1 finished!");
		}catch(Exception e){
			e.printStackTrace();
			Log.printLine("Unwanted errors happened!");
		}
	}

	//下面是创建数据中心的步骤
	private static Datacenter createDatacenter(String name){
		//1、创建主机列表
				List<Host> hostList = new ArrayList<Host>();
				//PE及其主要参数
				int mips = 1000;
				int hostId = 0;
				int ram = 2048;
				long storage = 1000000;
				int bw = 10000;
				for(int i = 0;i<vmNum;i++){
					//2、创建PE列表
					List<Pe> peList = new ArrayList<Pe>();
					
					//3、创建PE并加入列表
					peList.add(new Pe(0,new PeProvisionerSimple(mips)));
					
					//4、创建主机并加入列表
					hostList.add(new Host(hostId,new RamProvisionerSimple(ram),new BwProvisionerSimple(bw),
							storage,
							peList,
							new VmSchedulerTimeShared(peList))
					);
					hostId++;
				}
				
				//数据中心特征参数
				String arch = "x86";
				String os = "Linux";
				String vmm = "Xen";
				double time_zone = 10.0;
				double cost = 3.0;
				double costPerMem = 0.05;
				double costPerStorage = 0.001;
				double costPerBw = 0.0;
				LinkedList<Storage> storageList = new LinkedList<Storage>();
				
				//5、创建数据中心特征对象
				DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
						arch,os,vmm,hostList,time_zone,cost,costPerMem,costPerStorage,costPerBw);
				
				//6、创建数据中心对象
				Datacenter datacenter = null;
				try{
					datacenter = new Datacenter(name,characteristics,
							new VmAllocationPolicySimple(hostList),storageList,0);
				}catch(Exception e){
					e.printStackTrace();
				}
		return datacenter;
	}
	
	//创建数据中心代理
	private static DatacenterBroker createBroker(){
		DatacenterBroker broker = null;
		try{
			broker = new DatacenterBroker("Broker");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	//输出统计信息
	private static void printCloudletList(List<Cloudlet> cloudletlist){
		int size = cloudletlist.size();
		Cloudlet cloudlet;
		String indent = "	";
		Log.printLine();
		Log.printLine("==========OUTPUT==========");
		Log.printLine("Cloudlet ID"+indent+"STATUS"+indent+
				"Data center ID"+indent+"VM ID"+indent+
				"Time"+indent+"Start Time"+indent+"Finish Time");
		DecimalFormat dft = new DecimalFormat("###.##");
		for(int i = 0;i < size;i++){
			cloudlet = cloudletlist.get(i);
			Log.print(indent+cloudlet.getCloudletId()+indent+indent);
			if(cloudlet.getCloudletStatus()==Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				Log.printLine(indent+indent+cloudlet.getResourceId()+
						indent+indent+indent+cloudlet.getVmId()+
						indent+indent+dft.format(cloudlet.getActualCPUTime())+
						indent+indent+dft.format(cloudlet.getExecStartTime())+
						indent+indent+dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}

