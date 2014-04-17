package cloudStorage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class StorageSim {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/** The node number */
	private static int nodeNum;

	/** The nodes' properties */
	private static int ram = 2048; // host memory (MB)
	private static long storage = 1000000; // host storage
	private static int bw = 10000;
	private static int mips = 1000;
	private static int pesNumber = 1; // number of cpus

	/** The data to be read or write */
	private static List<BlockReplica> replicaList; // 副本列表
	private static int replicaNum = 10; // 由读数据时指定文件CloudFile来内部决定
	private static int cloudFileNum = 10;

	/**
	 * Creates main() to run this example.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting StorageSim...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// least one of them to run a CloudSim simulation
			nodeNum = 50;
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			// int mips = 1000;
			// long size = 10000; // image size (MB)
			long size = storage;
			// int ram = 512; // vm memory (MB)
			// long bw = 1000;

			String vmm = "Xen"; // VMM name

			for (int i = 0; i < nodeNum; i++) {
				// create VM
				Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size,
						vmm, new CloudletSchedulerTimeShared());
				vmid++;

				// add the VM to the vmList
				vmlist.add(vm);
			}

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// 增加对文件的支持
			replicaList = new ArrayList<BlockReplica>();

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			// Cloudlet properties
			int id = 0;
			long length = 250;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			// for(int i=0;i<nodeNum;i++){
			int replicaId = 1;
			for (int i = 1; i <= cloudFileNum; i++) {
				for (int j = 1; j <= replicaNum; j++) {
					BlockReplica replica = new BlockReplica(replicaId++,
							brokerId, "CloudFile" + i + "-blk1" + "-rep" + j);
					replica.init();

					replicaList.add(replica);

					Cloudlet cloudlet = new Cloudlet(id, length, pesNumber,
							fileSize, outputSize, utilizationModel,
							utilizationModel, utilizationModel);
					cloudlet.setUserId(brokerId);
					// cloudlet.setVmId(vmid);

					if (false == cloudlet.addRequiredFile(replica.getName())) {
						Log.printLine("Cloudlet " + cloudlet.getCloudletId()
								+ " add file " + replica.getName() + " failed!");
					}
					/*
					 * if(broker.getId()==cloudlet.getUserId()){
					 * broker.bindDataToDatacenter(replica); }
					 */
					datacenter0.addFile(replica);
					// add the cloudlet to the list
					cloudletList.add(cloudlet);
					id++;
				}
			}

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			// Print the debt of each user to each datacenter
			datacenter0.printDebts();

			Log.printLine("CloudSimStorage finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the datacenter
	 * @throws ParameterException
	 */
	private static Datacenter createDatacenter(String name)
			throws ParameterException {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		for (int i = 0; i < pesNumber; i++) {
			// 3. Create PEs and add these into a list.
			peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
																	// store Pe
																	// id and
																	// MIPS
																	// Rating
		}

		// 4. Create nodeNum Hosts with its id and list of PEs and add them to
		// the list
		// of machines
		int hostId = 0;

		for (int i = 0; i < nodeNum; i++) {
			hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw), storage, peList,
					// new VmSchedulerTimeShared(peList)
					new VmSchedulerSpaceShared(peList))); // This is our machine
			hostId++;
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		double networkLatency = 4.17;// ms
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
		// devices by now
		for (int i = 0; i < nodeNum; i++) {
			StorageNode node = new StorageNode(i, storage, bw, networkLatency,
					new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
					peList);
			i++;
			storageList.add(node);
		}

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0.2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
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
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
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
