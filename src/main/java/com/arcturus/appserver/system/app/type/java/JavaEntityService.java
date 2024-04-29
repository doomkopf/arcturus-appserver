package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityService;
import com.arcturus.api.tool.ClassToStringHasher;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;

import java.util.UUID;

public class JavaEntityService implements EntityService
{
	private final Logger log;
	private final ArcturusEntityService arcturusEntityService;
	private final JsonStringSerializer jsonStringSerializer;
	private final ClassToStringHasher classToStringHasher;

	public JavaEntityService(
		LoggerFactory loggerFactory,
		ArcturusEntityService arcturusEntityService,
		JsonStringSerializer jsonStringSerializer,
		ClassToStringHasher classToStringHasher
	)
	{
		log = loggerFactory.create(getClass());
		this.arcturusEntityService = arcturusEntityService;
		this.jsonStringSerializer = jsonStringSerializer;
		this.classToStringHasher = classToStringHasher;
	}

	@Override
	public void send(String useCase, UUID id, long requestId, UUID requestingUserId, String payload)
	{
		arcturusEntityService.send(useCase, id, requestId, requestingUserId, payload);
	}

	@Override
	public void sendObject(
		String useCase, UUID id, long requestId, UUID requestingUserId, Object payload
	)
	{
		try
		{
			send(
				useCase,
				id,
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(payload)
			);
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}

	@Override
	public void sendObject(
		Class<? extends com.arcturus.api.service.entity.EntityUseCaseHandler<?>> useCaseClass,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Object payload
	)
	{
		sendObject(
			classToStringHasher.classToString(useCaseClass),
			id,
			requestId,
			requestingUserId,
			payload
		);
	}
}