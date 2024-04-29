package com.arcturus.appserver.cluster;

/**
 * Containing the information to identify a node usually done by the ip and its
 * port. The port is necessary in case you have more than one node on a physical
 * machine.
 *
 * @author doomkopf
 */
public class NodeIdentity
{
	private final byte[] ip;
	private final int port;

	public NodeIdentity(byte[] ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	public boolean is(NodeIdentity nodeIdentity)
	{
		if (ip.length != nodeIdentity.ip.length)
		{
			return false;
		}

		for (var i = 0; i < ip.length; i++)
		{
			if (ip[i] != nodeIdentity.ip[i])
			{
				return false;
			}
		}

		return port == nodeIdentity.port;
	}
}