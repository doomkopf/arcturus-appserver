package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IMap;

import java.util.UUID;
import java.util.function.Consumer;

public class UserNodeContainer
{
	private static final String USER_ID_TO_NODE_MAP = "userIdToNodeMap";

	private final Logger log;
	private final IMap<UUID, UUID> userIdToNodeMap;

	public UserNodeContainer(
		LoggerFactory loggerFactory, Config config, SharedHazelcastInstance sharedHazelcastInstance
	)
	{
		log = loggerFactory.create(getClass());

		var hz = sharedHazelcastInstance.getHazelcastInstance();

		var mapConfig = new MapConfig(USER_ID_TO_NODE_MAP);
		mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
		mapConfig.setMaxIdleSeconds(config.getInt(ServerConfigPropery.userSessionCachesEvictionSeconds));
		var maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
		maxSizeConfig.setSize(config.getInt(ServerConfigPropery.hazelcastMaxUsedHeapSpacePercentage));
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setBackupCount(0);
		mapConfig.setAsyncBackupCount(1);

		hz.getConfig().addMapConfig(mapConfig);

		userIdToNodeMap = hz.getMap(USER_ID_TO_NODE_MAP);
	}

	public void put(UUID userId, UUID nodeId, Runnable completionHandler)
	{
		userIdToNodeMap.putAsync(userId, nodeId).andThen(new ExecutionCallback<>()
		{

			@Override
			public void onResponse(UUID response)
			{
				completionHandler.run();
			}

			@Override
			public void onFailure(Throwable t)
			{
				log.log(LogLevel.error, t);
				completionHandler.run();
			}
		});
	}

	void getNodeIdByUserId(UUID userId, Consumer<UUID> consumer)
	{
		userIdToNodeMap.getAsync(userId).andThen(new ExecutionCallback<>()
		{
			@Override
			public void onResponse(UUID response)
			{
				if (response == null)
				{
					consumer.accept(null);
					return;
				}

				consumer.accept(response);
			}

			@Override
			public void onFailure(Throwable t)
			{
				log.log(LogLevel.error, t);
				consumer.accept(null);
			}
		});
	}
}
