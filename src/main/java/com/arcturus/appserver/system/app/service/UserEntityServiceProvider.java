package com.arcturus.appserver.system.app.service;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.EntityServiceFactory;
import com.arcturus.appserver.system.app.service.info.ServiceInfos;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class UserEntityServiceProvider
{
	public static final String MSG_SERVICE_NOT_FOUND = "Service not found: ";

	private final ServiceInfos serviceInfos;

	private final Logger log;
	private final Map<String, ArcturusEntityService> nameToServiceMap = new HashMap<>();

	public UserEntityServiceProvider(
		LoggerFactory loggerFactory, ServiceInfos serviceInfos
	)
	{
		log = loggerFactory.create(getClass());
		this.serviceInfos = serviceInfos;
	}

	public void init(EntityServiceFactory<?> entityServiceFactory)
		throws ArcturusAppException, UnknownHostException
	{
		try
		{
			for (var serviceName : serviceInfos.getEntityServiceNames())
			{
				nameToServiceMap.put(serviceName, entityServiceFactory.create(serviceName, this));
			}
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
			shutdown();
			throw e;
		}
	}

	public ArcturusEntityService getServiceByName(String name)
	{
		return nameToServiceMap.get(name);
	}

	public void shutdown()
	{
		for (var service : nameToServiceMap.values())
		{
			try
			{
				service.shutdown();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}
	}
}