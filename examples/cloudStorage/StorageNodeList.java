package cloudStorage;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.lists.PeList;

public class StorageNodeList {


	/**
	 * Gets the Machine object for a particular ID.
	 *
	 * @param id    the host ID
	 * @param hostList the host list
	 *
	 * @return the Machine object or <tt>null</tt> if no machine exists
	 *
	 * @see gridsim.Machine
	 * @pre id >= 0
	 * @post $none
	 */
    public static <T extends StorageNode> T getById(List<T> nodeList, int id) {
        for (T node : nodeList) {
    		if (node.getId() == id) {
    			return node;
    		}
		}
        return null;
    }

    /**
     * Gets the total number of PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends StorageNode> int getPesNumber(List<T> nodeList) {
        int pesNumber = 0;
        for (T node : nodeList) {
    		pesNumber += node.getPeList().size();
		}
        return pesNumber;
    }

    /**
     * Gets the total number of <tt>FREE</tt> or non-busy PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends StorageNode> int getFreePesNumber(List<T> nodeList) {
        int freePesNumber = 0;
        for (T node : nodeList) {
        	freePesNumber += PeList.getFreePesNumber((List<Pe>) node.getPeList());
		}
        return freePesNumber;
    }

    /**
     * Gets the total number of <tt>BUSY</tt> PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
   /* @SuppressWarnings("unchecked")
	public static <T extends StorageNode> int getBusyPesNumber(List<T> nodeList) {
        int busyPesNumber = 0;
        for (T node : nodeList) {
        	busyPesNumber += PeList.getBusyPesNumber((List<Pe>) node.getPeList());
		}
        return busyPesNumber;
    }*/

    /**
     * Gets a Machine with free Pe.
     *
     * @param hostList the host list
     *
     * @return a machine object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    public static <T extends StorageNode> T getNodeWithFreePe(List<T> nodeList) {
        return getNodeWithFreePe(nodeList, 1);
    }

    /**
     * Gets a Machine with a specified number of free Pe.
     *
     * @param pesNumber the pes number
     * @param hostList the host list
     *
     * @return a machine object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    @SuppressWarnings("unchecked")
	public static <T extends StorageNode> T getNodeWithFreePe(List<T> nodeList, int pesNumber) {
        for (T node : nodeList) {
        	if (PeList.getFreePesNumber((List<Pe>) node.getPeList()) >= pesNumber) {
        		return node;
        	}
		}
        return null;
    }

    /**
     * Sets the particular Pe status on a Machine.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param hostId the host id
     * @param peId the pe id
     * @param hostList the host list
     *
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Machine id or Pe id might not be exist)
     *
     * @pre machineID >= 0
     * @pre peID >= 0
     * @post $none
     */
    public static <T extends StorageNode> boolean setPeStatus(List<T> nodeList, int status, int nodeId, int peId) {
        T node = getById(nodeList, nodeId);
        if (node == null) {
            return false;
        }
        return node.setPeStatus(peId, status);
    }
}
