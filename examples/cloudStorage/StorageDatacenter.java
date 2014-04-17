package cloudStorage;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * @author huahero
 * @description Ϊ�˷�ֹ��ԭCloudSim-2-1-1����Ӧ��Դ������̫��Ķ������Ծ����������ļ���
 *              �ڼ̳�ԭ����Ҫ����Ļ����ϣ������Լ����µĶ���
 *              �����ԭDatacenter�м����˶Դ洢�ڵ㼯���ԣ���Ϊģ���ƴ洢�зŸ���Ҫ�õ����������˶Դ洢�ڵ�洢״����Ϣ�����
 */

public class StorageDatacenter extends Datacenter {

	public List<? extends StorageNode> nodeList;
	public StorageNodeList NodeList;
	
	public double totalMIPS;
	public double totalMem;
	public double totalCapacity;

	@SuppressWarnings("unchecked")
	public StorageDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// TODO Auto-generated constructor stub
		this.nodeList = (List<? extends StorageNode>) storageList;
		totalMIPS = getTotalMIPS(nodeList);
		totalMem = getTotalMem(nodeList);
		totalCapacity = getTotalCapacity(nodeList);
	}

	/**
	 * des:prints the storage information of the datacenter Problem exists
	 * there:"thread out of sync",which means We cannot know what code is
	 * executing in a running thread, so the best that we can do is to say that
	 * a running thread "may be out of synch" after an HCR fails
	 * details����http://
	 * dev.eclipse.org/mhonarc/lists/eclipse-announce/msg00021.html
	 */
	public void printStorageInfo() {
		int nodeSize = this.storageList.size();
		String indent = "		";
		Log.printLine("StorageNode  Name:" + indent + "Id:" + indent + " Used:"
				+ indent + "Capacity:" + indent + "Usage:");
		double totalCurSize = 0,totalCap = 0;
		for (int i = 0; i < nodeSize; i++) {
			StorageNode node = (StorageNode) this.storageList.get(i);
			double curSize = node.getCurrentSize();
			double cap = node.getCapacity();
			double usage = curSize / cap;
			totalCurSize += curSize;
			totalCap += cap;
			Log.printLine(node.getName() + indent + node.getId() + indent
					+ curSize + indent + cap + indent + usage);
		}
		Log.printLine("totalStored:" + totalCurSize + indent + "totalCapcity:"
				+ totalCap + indent + "AverageStorageUseage:" + totalCurSize
				/ totalCap);
	}
	
	/**
	 * get the total mips of this center with this center's node list
	 * @param nodeList
	 * @return
	 */
	public double getTotalMIPS(List<? extends StorageNode> nodeList){
		double sumMips = 0.0;
		int size = nodeList.size();
		for(int i=0;i<size;i++){
			sumMips += nodeList.get(i).getPeList().get(0).getMips();//we just give every node one Pe standard
		}
		return sumMips;
	}
	
	/**
	 * Gets the total memory of this center with this center's node list
	 * @param nodeList
	 * @return
	 */
	public double getTotalMem(List<? extends StorageNode> nodeList){
		double sumMem = 0.0;
		int size = nodeList.size();
		for(int i=0;i<size;i++){
			sumMem += nodeList.get(i).getRamProvisioner().getAvailableRam();
		}
		return sumMem;
	}
	
	public double getTotalCapacity(List<? extends StorageNode> nodeList){
		double sumCapacity = 0.0;
		int size = nodeList.size();
		for(int i=0;i<size;i++){
			sumCapacity += nodeList.get(i).getCapacity();
		}
		return sumCapacity;
	}
}
