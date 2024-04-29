package com.arcturus.appserver.cluster;

import java.util.UUID;

/**
 * The cluster the current VM is part of. Each {@link Node} is identified by a
 * transient {@link UUID} that is only valid during the {@link Node}s uptime.
 *
 * @author doomkopf
 */
public interface Cluster
{
	Node getLocalNode();

	Node getNodeById(UUID id);

	/**
	 * @return A threadsafe {@link Iterable}.
	 */
	Iterable<UUID> getAllNodesIterable();

	int getNodeCount();
}