package cloudStorage;

import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;

public class StorageDatacenterCharacteristics extends DatacenterCharacteristics{
	/**The storage list*/
//	protected List<? extends StorageNode> nodeList;
	public List<? extends StorageNode> nodeList;

	public StorageDatacenterCharacteristics(String architecture, String os,
			String vmm, List<? extends Host> hostList,List<? extends StorageNode> nodeList2, double timeZone,
			double costPerSec, double costPerMem, double costPerStorage,
			double costPerBw) {
		super(architecture, os, vmm, hostList, timeZone, costPerSec, costPerMem,
				costPerStorage, costPerBw);
		// TODO Auto-generated constructor stub
		setNodeList(nodeList2);
	}

	public List<? extends StorageNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<? extends StorageNode> nodeList) {
		this.nodeList = nodeList;
	}

	@Override
	protected int getId() {
		// TODO Auto-generated method stub
		return super.getId();
	}
	
	

}
