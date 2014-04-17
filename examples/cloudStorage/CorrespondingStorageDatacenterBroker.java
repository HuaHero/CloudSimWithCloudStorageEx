package cloudStorage;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * My idea for this broker class is that:implement a strategy that create the virtual machines
 * (vms) in datacenter that the number of hosts corresponding the number of vms .To make this
 * of course we can modify the creaeVmsInDatacenter() of DatacenterBroker.But to insure to limit
 * the effect of modifying the core of CloudSim,I choose to extends strategy
 * @author HuaHero
 * This program is in CloudSim-2.1.1,and as I want to implement the situation similar to 
 * Federated Cloud and have learnt that the class CloudCoordinator 
 * (https://code.google.com/p/cloudsim/source/browse/tags/cloudsim-2.1.1/modules/cloudsim/src/main/java/org/cloudbus/cloudsim/CloudCoordinator.java)
 * doesn't exist anymore (https://groups.google.com/forum/#!searchin/cloudsim/Fderated/cloudsim/Y35nz4grJPY/77uWf9uWLuYJ),so 
 * to compatible with later release version of CloudSim,I choose to  implement special DatacenterBroker
 */
public class CorrespondingStorageDatacenterBroker extends StorageDatacenterBroker{

//	private double[][] delayMatrixAcrossRegion;
	 /**
     * Creates an instance of this class associating to it a given name.
     * @param name The name to be associated to this broker. It might not be <code>null</code> or empty.
     * @throws Exception If the name contains spaces.
     */
	public CorrespondingStorageDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	//when get the resouce characteristics answer 
	protected void processResourceCharacteristics(SimEvent ev) {
		// TODO Auto-generated method stub
		StorageDatacenterCharacteristics characteristics = (StorageDatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);
        
        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()){
        	 setDatacenterRequestedIdsList(new ArrayList<Integer>());
             distributeVMsAcrossCorrsepondingNum();
        }
	}

	protected void distributeVMsAcrossCorrsepondingNum(){
		int numOfVmsAllocated = 0;
		int i = 0;
		
		final List<Integer> availableDatacenters = getDatacenterIdsList();
		final int availableDataSize = availableDatacenters.size();
		
		for(int j=0;j<availableDataSize;j++){
			//datacenter(j)'s node scale
			Datacenter dc = (Datacenter)CloudSim.getEntity(getDatacenterIdsList().get(j));
			int nodeSize = dc.getHostList().size();
			int dcId = dc.getId();
			String dcName = dc.getName();
			for(int k=0;k < nodeSize;k++){
				int vmId = getVmList().get(i).getId();//the vmList(i)'s id
				if(!getVmsToDatacentersMap().containsKey(vmId)){
					Log.printLine(CloudSim.clock()+":"+getName()+":Trying to Create VM#" + vmId + " in "+dcName);
					sendNow(dcId,CloudSimTags.VM_CREATE_ACK,getVmList().get(i));
					i++;
					numOfVmsAllocated++;
				}
			}
			getDatacenterRequestedIdsList().add(dcId);
		}
		setVmsRequested(numOfVmsAllocated);
		setVmsAcks(0);
	}
	
}
