package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.list.ListServiceProvider;
import com.arcturus.appserver.system.Tools;

import java.util.UUID;

public class JsListServices
{
	private final Logger log;
	private final ListServiceProvider listServiceProvider;

	public JsListServices(LoggerFactory loggerFactory, ListServiceProvider listServiceProvider)
	{
		log = loggerFactory.create(getClass());
		this.listServiceProvider = listServiceProvider;
	}

	public void add(String serviceName, String elemToAdd, String entityId)
	{
		var service = listServiceProvider.getServiceByName(serviceName);
		if (service == null)
		{
			logUnknownService(serviceName);
			return;
		}

		service.add(elemToAdd, UUID.fromString(entityId));
	}

	public void remove(String serviceName, String elemToRemove, String entityId)
	{
		var service = listServiceProvider.getServiceByName(serviceName);
		if (service == null)
		{
			logUnknownService(serviceName);
			return;
		}

		service.remove(elemToRemove, UUID.fromString(entityId));
	}

	public void collect(
		String serviceName,
		String finalUseCaseId,
		String entityId,
		String requestId,
		String requestingUserId
	)
	{
		var service = listServiceProvider.getServiceByName(serviceName);
		if (service == null)
		{
			logUnknownService(serviceName);
			return;
		}

		service.collect(
			finalUseCaseId,
			UUID.fromString(entityId),
			Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			(requestingUserId == null) ? null : UUID.fromString(requestingUserId)
		);
	}

	private void logUnknownService(String serviceName)
	{
		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(LogLevel.debug, "Unknown service: " + serviceName);
		}
	}
}