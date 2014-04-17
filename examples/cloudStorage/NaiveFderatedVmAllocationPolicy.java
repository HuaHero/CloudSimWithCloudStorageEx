package cloudStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
/**
* @author HuaHero
*This class is to implement a vm allocation policy considering the federation.
*1st,get all datacenters-implemented in super class
*2nd,get the host list of each datacenter 
*3rd,allocate the vms considering the thresholds
*/
public class NaiveFderatedVmAllocationPolicy extends VmAllocationPolicySimple{

	public NaiveFderatedVmAllocationPolicy(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Returns all{@link Datacenter}s created for this simulation
	 * @return A {@link List} with all data centers created for this simulation
	 */
	protected List<Datacenter> getDatacenters(){
		List<Datacenter> datacenters = new ArrayList<Datacenter>();
		for(SimEntity simEntity:CloudSim.getEntityList()){
			if(simEntity instanceof Datacenter){
				datacenters.add((Datacenter)simEntity);
			}
		}
		return datacenters;
	}
	
	/**get the host list of each datacenter*/
	public <T extends Host> List<T> getHostList(){
		Map<Integer,T> hosts = new HashMap<Integer,T>();
		
		for(Datacenter datacenter:this.getDatacenters()){
			for(T host:datacenter.<T>getHostList()){
				hosts.put(host.getId(),host);
			}
		}
		return new ArrayList<T>(hosts.values());
	}

	/*-----------allocate the vms considering thresholds or sth-------------*/
	
	@Override
	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		Host allocatedHost = findHostForVm(vm);
		if(allocatedHost !=null && allocatedHost.vmCreate(vm)){
			this.getVmTable().put(vm.getUid(),allocatedHost);
			return true;
		}
		return false;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		if(getVmTable().containsKey(vm.getUid())){
			Host host = getVmTable().remove(vm.getUid());
			if(host!=null){
				host.vmDestroy(vm);
			}
		}
	}
	
	
	protected Host findHostForVm(Vm vm){
		for(Host host:this.getHostList()){
			if(host.isSuitableForVm(vm)){
				return host;
			}
		}
		return null;
	}
}
