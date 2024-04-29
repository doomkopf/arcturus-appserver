package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.cluster.NodeIdentity;
import com.arcturus.appserver.system.NodeIdentityList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class EntityIdToNodeBalancerTest
{
	private static class TestNode implements Node
	{
		final UUID id;
		final NodeIdentity nodeIdentity;
		final boolean isLocal;

		TestNode(UUID id, NodeIdentity nodeIdentity, boolean isLocal)
		{
			this.id = id;
			this.nodeIdentity = nodeIdentity;
			this.isLocal = isLocal;
		}

		@Override
		public UUID getId()
		{
			return id;
		}

		@Override
		public NodeIdentity getPhysicalIdentity()
		{
			return nodeIdentity;
		}

		@Override
		public boolean isLocal()
		{
			return isLocal;
		}

		@Override
		public void send(byte[] byteData)
		{
			// Nothing
		}
	}

	@Test
	void testFilterAndFindMinLoadNode() throws UnknownHostException
	{
		var ip = InetAddress.getByName("192.168.1.10").getAddress();

		Node localNode;

		Map<UUID, Node> nodes = new HashMap<>();
		UUID nodeId;

		nodeId = new UUID(0, 0);
		localNode = new TestNode(nodeId, new NodeIdentity(ip, 1234), true);
		nodes.put(nodeId, localNode);

		nodeId = new UUID(0, 1);
		nodes.put(nodeId, new TestNode(nodeId, new NodeIdentity(ip, 1235), true));

		Cluster cluster = new Cluster()
		{
			@Override
			public Node getNodeById(UUID id)
			{
				return nodes.get(id);
			}

			@Override
			public Node getLocalNode()
			{
				return localNode;
			}

			@Override
			public Iterable<UUID> getAllNodesIterable()
			{
				return nodes.keySet();
			}

			@Override
			public int getNodeCount()
			{
				return nodes.size();
			}
		};

		var nodeIdentityList = new NodeIdentityList(true, new String[] {"192.168.1.10:1235"});

		var entityIdToNodeBalancer = new EntityIdToNodeBalancer(cluster, nodeIdentityList);

		nodeId = entityIdToNodeBalancer.filterAndFindMinLoadNode();

		Assertions.assertEquals(1, nodeId.getLeastSignificantBits());
	}
}