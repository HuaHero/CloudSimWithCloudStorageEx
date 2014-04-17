package cloudStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;

/**
 * This program is in CloudSim-2.1.1,and as I want to implement the situation similar to 
 * Federated Cloud and have learnt that the class CloudCoordinator 
 * (https://code.google.com/p/cloudsim/source/browse/tags/cloudsim-2.1.1/modules/cloudsim/src/main/java/org/cloudbus/cloudsim/CloudCoordinator.java)
 * doesn't exist anymore (https://groups.google.com/forum/#!searchin/cloudsim/Fderated/cloudsim/Y35nz4grJPY/77uWf9uWLuYJ),so 
 * to compatible with later release version of CloudSim,I choose to  implement special DatacenterBroker
 * @author HuaHero
 *This class is to implement a vm allocation policy considering the federation.
 *1st,get all datacenters-implemented in super class
 *2nd,get the host list of each datacenter 
 *3rd,allocate the vms considering the thresholds
 */
public class RoundRobinStorageDatacenterBroker extends StorageDatacenterBroker{
	/**The host list which will be used to allocated virtual machines,vms*/
	protected List<? extends Host> hostList;

	public RoundRobinStorageDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
		setHostList(new ArrayList<Host>());
	}
	/**set the host list manually*/
	public void setHostList(List<Host> hostLst){
		this.hostList = hostLst;
	}
	/**get the host list of each datacenter*/
	public <T extends Host> List<T> getHostList(){
		Map<Integer,T> hosts = new HashMap<Integer,T>();
		
		for(StorageDatacenter datacenter:this.getDatacenterList()){
			for(T host:datacenter.<T>getHostList()){
				hosts.put(host.getId(),host);
			}
		}
		return new ArrayList<T>(hosts.values());
	}
	
	/**allocate the vms considering thresholds or sth*/
	

}
