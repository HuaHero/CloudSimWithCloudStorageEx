package cloudStorage;

import java.util.List;


public class ReplicaList {
	/**
	 * Return a reference to a BlockReplica object from its ID.
	 *
	 * @param id ID of required BlockReplica
	 * @param replicaList the BlockReplica list
	 *
	 * @return BlockReplica with the given ID, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public static <T extends BlockReplica> T getById(List<T> replicaList, int id) {
		for (T replica : replicaList) {
			if (replica.getId() == id) {
				return replica;
			}
		}
		return null;
	}

	/**
	 * Return a reference to a BlockReplica object from its ID and user ID.
	 *
	 * @param id ID of required BlockReplica
	 * @param userId the user ID
	 * @param replicaList the replica list
	 *
	 * @return BlockReplica with the given ID, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public static <T extends BlockReplica> T getByIdAndUserId(List<T> replicaList, int id, int userId) {
		for (T replica : replicaList) {
			if (replica.getId() == id && replica.getUserId() == userId) {
				return replica;
			}
		}
		return null;
	}
}
