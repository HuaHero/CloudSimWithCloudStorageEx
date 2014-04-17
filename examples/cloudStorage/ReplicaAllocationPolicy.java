package cloudStorage;

import java.util.List;
import java.util.Map;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

public abstract class ReplicaAllocationPolicy {
	
	/** The host list. */
	private List<? extends StorageNode> nodeList;

	/**
	 * Allocates a new ReplicaAllocationPolicy object.
	 *
	 * @param list Machines available in this Datacenter
	 *
	 * @pre $none
	 * @post $none
	 */
	public ReplicaAllocationPolicy(List<? extends StorageNode> list) {
		setNodeList(list);
	}

	/**
	 * Allocates a host for a given BlockReplica. The host to be allocated is the one
	 * that was already reserved.
	 *
	 * @param replica replica which the host is reserved to
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateNodeForReplica(BlockReplica replica);

	/**
	 * Allocates a specified host for a given BlockReplica.
	 *
	 * @param repic replica which the host is reserved to
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateNodeForReplica(BlockReplica replica, StorageNode node);

	/**
	 * Optimize allocation of the BlockReplicas according to current utilization.
	 *
	 * @param replicaList the replica list
	 * @param utilizationBound the utilization bound
	 * @param time the time
	 *
	 * @return the array list< hash map< string, object>>
	 */
	public abstract List<Map<String, Object>> optimizeAllocation(List<? extends BlockReplica> replicaList);

	/**
	 * Releases the node used by a BlockReplica.
	 *
	 * @param replica the replica
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocateNodeForReplica(BlockReplica replica);

	/**
	 * Get the host that is storing the given BlockReplica belonging to the
	 * given user.
	 *
	 * @param replica the replica
	 *
	 * @return the Host with the given replica; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract StorageNode getNode(BlockReplica replica);

	/**
	 * Get the host that is storing the given BlockReplica belonging to the
	 * given user.
	 *
	 * @param replicaId the replica id
	 * @param userId the user id
	 *
	 * @return the Host with the given vmID and userID; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract StorageNode getNode(int replicaId, int userId);

	/**
	 * Sets the host list.
	 *
	 * @param hostList the new host list
	 */
	protected void setNodeList(List<? extends StorageNode> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * Gets the host list.
	 *
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends StorageNode> List<T> getNodeList() {
		return (List<T>) nodeList;
	}
}


