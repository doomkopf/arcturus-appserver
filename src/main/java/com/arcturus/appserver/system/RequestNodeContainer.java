package com.arcturus.appserver.system;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class RequestNodeContainer
{
	private final Cache<Long, UUID> requestIdToNodeIdMap;

	public RequestNodeContainer(Config config)
	{
		requestIdToNodeIdMap = CacheBuilder
				.newBuilder()
				.expireAfterWrite(
						config.getInt(ServerConfigPropery.requestIdCachesEvictionSeconds),
						TimeUnit.SECONDS)
				.build();
	}

	public UUID getNodeIdByRequestId(long requestId)
	{
		return requestIdToNodeIdMap.getIfPresent(Long.valueOf(requestId));
	}

	public void put(long requestId, UUID nodeId)
	{
		requestIdToNodeIdMap.put(Long.valueOf(requestId), nodeId);
	}
}