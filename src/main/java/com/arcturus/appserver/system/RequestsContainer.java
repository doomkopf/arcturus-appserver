package com.arcturus.appserver.system;

import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.net.RequestContext;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class RequestsContainer
{
	private final Cache<Long, RequestContext> requestIdToContextMap;

	public RequestsContainer(Config config)
	{
		requestIdToContextMap = CacheBuilder.newBuilder()
			.expireAfterWrite(config.getInt(ServerConfigPropery.requestIdCachesEvictionSeconds),
				TimeUnit.SECONDS
			)
			.build();
	}

	public void put(long requestId, RequestContext requestContext)
	{
		requestIdToContextMap.put(Long.valueOf(requestId), requestContext);
	}

	public RequestContext remove(long requestId)
	{
		var requestIdLong = Long.valueOf(requestId);
		var requestContext = requestIdToContextMap.getIfPresent(requestIdLong);
		if (requestContext != null)
		{
			requestIdToContextMap.invalidate(requestIdLong);
		}

		return requestContext;
	}
}