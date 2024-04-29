package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;

import java.util.UUID;

public class JsServices
{
	private final Logger log;
	private final UserEntityServiceProvider serviceProvider;

	public JsServices(LoggerFactory loggerFactory, UserEntityServiceProvider serviceProvider)
	{
		log = loggerFactory.create(getClass());
		this.serviceProvider = serviceProvider;
	}

	public void send(
		String serviceName,
		String useCase,
		String id,
		String requestId,
		String requestingUserId,
		String jsJsonPayload
	)
	{
		var service = serviceProvider.getServiceByName(serviceName);
		if (service == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Unknown service: " + serviceName);
			}
			return;
		}

		service.send(
			useCase,
			UUID.fromString(id),
			(requestId == null) ?
				0 :
				Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			(requestingUserId == null) ? null : UUID.fromString(requestingUserId),
			jsJsonPayload
		);
	}
}