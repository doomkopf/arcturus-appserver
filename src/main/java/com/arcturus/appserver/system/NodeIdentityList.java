package com.arcturus.appserver.system;

import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.cluster.NodeIdentity;
import com.hazelcast.util.AddressUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Parses the hosts of the service configs to match them indepent of DNS or
 * direct IP addresses. Offers convenience methods for determining whether a
 * node is part of this list or not. Used for determining on which node a
 * service is supposed to be located on.
 *
 * @author doomkopf
 */
public class NodeIdentityList
{
	private static byte[] hostToIpBytes(String host) throws UnknownHostException
	{
		if (!AddressUtil.isIpAddress(host))
		{
			host = InetAddress.getByName(host).getHostAddress();
		}

		return InetAddress.getByName(host).getAddress();
	}

	private static NodeIdentity hostToNodeIdentity(String host) throws UnknownHostException
	{
		var hostAndPort = host.split(":");
		var port = -1;
		if (hostAndPort.length == 2)
		{
			port = Integer.parseInt(hostAndPort[1]);
		}

		return new NodeIdentity(hostToIpBytes(hostAndPort[0]), port);
	}

	private static Collection<NodeIdentity> hostsToNodeIdentities(String[] nodeHosts)
		throws UnknownHostException
	{
		Collection<NodeIdentity> nodes = new ArrayList<>(nodeHosts.length);
		for (var nodeHost : nodeHosts)
		{
			nodes.add(hostToNodeIdentity(nodeHost));
		}

		return nodes;
	}

	private final boolean isInclusionList;
	private final Collection<NodeIdentity> nodeIdentities;

	public NodeIdentityList(boolean isInclusionList, String[] nodeHosts) throws UnknownHostException
	{
		this.isInclusionList = isInclusionList;
		nodeIdentities = ((nodeHosts.length == 0) || nodeHosts[0].isEmpty()) ?
			new ArrayList<>(0) :
			hostsToNodeIdentities(nodeHosts);
	}

	public Iterable<NodeIdentity> getNodeIdentitiesIterable()
	{
		return nodeIdentities;
	}

	public boolean isInclusionList()
	{
		return isInclusionList;
	}

	public int size()
	{
		return nodeIdentities.size();
	}

	public boolean isIncluded(Node node)
	{
		var containsNode = containsNode(node);
		return isInclusionList == containsNode;
	}

	private boolean containsNode(Node node)
	{
		if (node == null)
		{
			return false;
		}

		return nodeIdentities.stream()
			.anyMatch(nodeIdentity -> nodeIdentity.is(node.getPhysicalIdentity()));
	}
}