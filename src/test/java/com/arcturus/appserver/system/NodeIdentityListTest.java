package com.arcturus.appserver.system;

import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.cluster.NodeIdentity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

class NodeIdentityListTest
{
	@Test
	void testIsIncluded() throws UnknownHostException
	{
		var nodeIdentityList = new NodeIdentityList(true, new String[] {"192.168.1.10:2"});

		var test = new NodeIdentity(InetAddress.getByName("192.168.1.10").getAddress(), 1);

		var isIncluded = nodeIdentityList.isIncluded(new Node()
		{
			@Override
			public void send(byte[] byteData)
			{
				// Nothing
			}

			@Override
			public boolean isLocal()
			{
				return false;
			}

			@Override
			public NodeIdentity getPhysicalIdentity()
			{
				return test;
			}

			@Override
			public UUID getId()
			{
				return null;
			}
		});

		Assertions.assertFalse(isIncluded);
	}
}