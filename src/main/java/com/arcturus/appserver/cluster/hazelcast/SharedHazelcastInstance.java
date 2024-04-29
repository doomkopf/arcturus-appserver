package com.arcturus.appserver.cluster.hazelcast;

import com.arcturus.api.LogLevel;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.hazelcast.serializer.UUIDStreamSerializer;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.LoggerType;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.system.Tools;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;
import java.util.UUID;

/**
 * Wraps the global {@link HazelcastInstance} and configures it.
 *
 * @author doomkopf
 */
public class SharedHazelcastInstance
{
	private final HazelcastInstance hz;

	public SharedHazelcastInstance(Config config, LoggerFactory loggerFactory)
	{
		var hzConfig = new com.hazelcast.config.Config();

		var log = loggerFactory.create(getClass());

		var networkConfig = hzConfig.getNetworkConfig();
		var port = config.getInt(ServerConfigPropery.hazelcastPort);
		if (port > 0)
		{
			networkConfig.setPort(port);
		}

		List<String> memberHosts = null;
		var stringMemberHosts = config.getString(ServerConfigPropery.hazelcastMembers);
		if ((stringMemberHosts != null) && !stringMemberHosts.isEmpty())
		{
			memberHosts = Tools.arrayToList(stringMemberHosts.split(","));
		}

		if ((memberHosts != null) && !memberHosts.isEmpty())
		{
			var joinConfig = networkConfig.getJoin();
			joinConfig.getMulticastConfig().setEnabled(false);
			var tcpIpConfig = joinConfig.getTcpIpConfig();
			tcpIpConfig.setEnabled(true);
			tcpIpConfig.setMembers(memberHosts);
		}
		else
		{
			log.log(LogLevel.info,
				"No hazelcast member configuration found - using default multicast"
			);
		}

		var loggerType = config.getEnum(LoggerType.class, ServerConfigPropery.logger);
		if (loggerType == LoggerType.log4j2)
		{
			hzConfig.setProperty("hazelcast.logging.type", "log4j2");
		}

		hzConfig.setProperty("hazelcast.jmx", "true");
		hzConfig.setProperty("hazelcast.shutdownhook.enabled", "false");

		hzConfig.getGroupConfig().setName(config.getString(ServerConfigPropery.hazelcastGroupName));

		hzConfig.getSerializationConfig()
			.addSerializerConfig(new SerializerConfig().setImplementation(new UUIDStreamSerializer())
				.setTypeClass(UUID.class));

		hz = Hazelcast.newHazelcastInstance(hzConfig);
	}

	public HazelcastInstance getHazelcastInstance()
	{
		return hz;
	}

	public void shutdown()
	{
		hz.shutdown();
	}
}