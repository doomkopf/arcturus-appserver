package com.arcturus.appserver.cluster;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.hazelcast.HazelcastMember;
import com.arcturus.appserver.system.RemoteMessageHandler;

/**
 * Passes messages received from other nodes to {@link RemoteMessageHandler}.
 *
 * @author doomkopf
 */
public class RemoteMessageReceiver
{
	private final Logger log;
	private final RemoteMessageHandler remoteMessageHandler;

	public RemoteMessageReceiver(
		LoggerFactory loggerFactory, RemoteMessageHandler remoteMessageHandler
	)
	{
		log = loggerFactory.create(getClass());
		this.remoteMessageHandler = remoteMessageHandler;

		// Hack to make hazelcast remote execution working:
		// Accessed by deserialized messages from remote nodes
		HazelcastMember.remoteMessageReceiver = this;
	}

	public void handleMessage(byte[] byteData)
	{
		try
		{
			remoteMessageHandler.handleMessage(byteData);
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}
}