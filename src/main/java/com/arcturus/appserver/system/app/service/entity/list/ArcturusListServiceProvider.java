package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.api.service.entity.list.ListServiceProvider;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.list.ListChunkEntityServiceFactory.ListChunkEntityServiceResult;
import com.arcturus.appserver.system.app.service.info.ServiceInfos;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ArcturusListServiceProvider implements ListServiceProvider
{
	private final Logger log;

	private final Map<String, ListChunkEntityServiceResult<?>> nameToServiceMap = new HashMap<>();

	public ArcturusListServiceProvider(
		LoggerFactory loggerFactory,
		ServiceInfos serviceInfos,
		ListChunkEntityServiceFactory listChunkEntityServiceFactory,
		JsonStringSerializer jsonStringSerializer
	) throws UnknownHostException
	{
		log = loggerFactory.create(getClass());

		for (var listServiceConfig : serviceInfos.getListServiceConfigIterable())
		{
			nameToServiceMap.put(listServiceConfig.name(), listChunkEntityServiceFactory.create(
				listServiceConfig.name(),
				listServiceConfig.listChunkEntityType(),
				listServiceConfig.collectElementsMessageType(),
				new AutoListElementTypeSerializer((Class) listServiceConfig.listElementType(),
					jsonStringSerializer
				),
				null
			));
		}
	}

	public <T> ArcturusListChunkService<T> getInternalServiceByName(String name)
	{
		var service = nameToServiceMap.get(name);
		if (service == null)
		{
			return null;
		}

		return (ArcturusListChunkService<T>) service.listService;
	}

	@Override
	public <T> ListService<T> getServiceByName(String name)
	{
		return getInternalServiceByName(name);
	}

	void shutdown()
	{
		for (var service : nameToServiceMap.values())
		{
			try
			{
				service.entityService.shutdown();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}
	}
}