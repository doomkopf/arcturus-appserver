package com.arcturus.appserver.system;

import com.arcturus.appserver.cluster.Cluster;

import java.util.UUID;

public class SourceNodeIdProvider
{
	private final RequestNodeContainer requestNodeContainer;
	private final UUID localNodeId;

	public SourceNodeIdProvider(RequestNodeContainer requestNodeContainer, Cluster cluster)
	{
		this.requestNodeContainer = requestNodeContainer;
		localNodeId = cluster.getLocalNode().getId();
	}

	public UUID getSourceNodeId(long requestId)
	{
		var nodeId = requestNodeContainer.getNodeIdByRequestId(requestId);
		if (nodeId != null)
		{
			return nodeId;
		}

		return localNodeId;
	}
}