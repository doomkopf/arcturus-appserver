package com.arcturus.appserver.system.app.service.entity.list.usecase;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.service.entity.list.CollectFinalMessage;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.ListUseCase;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements.CollectElementsMessage;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class CollectElements<T>
	extends PojoPayloadEntityUseCaseHandler<ListChunk<T>, CollectElementsMessage<T>>
{
	public static class CollectElementsMessage<T>
	{
		String endUc;
		Collection<T> elems;

		CollectElementsMessage()
		{
		}

		public CollectElementsMessage(String endUc)
		{
			this.endUc = endUc;
			elems = new LinkedList<>();
		}
	}

	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final ArcturusEntityService entityService;
	private final UseCaseProcessor useCaseProcessor;
	private final Type collectElementsMessageType;

	public CollectElements(
		LoggerFactory loggerFactory,
		JsonStringSerializer jsonStringSerializer,
		ArcturusEntityService entityService,
		UseCaseProcessor useCaseProcessor,
		Type collectElementsMessageType
	)
	{
		super(jsonStringSerializer);
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.entityService = entityService;
		this.useCaseProcessor = useCaseProcessor;
		this.collectElementsMessageType = collectElementsMessageType;
	}

	@Override
	protected Type getPayloadType()
	{
		return collectElementsMessageType;
	}

	@Override
	protected void handle(
		ListChunk<T> entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		CollectElementsMessage<T> message,
		UseCaseContext context
	)
	{
		if (entity != null)
		{
			for (var elem : entity)
			{
				message.elems.add(elem);
			}
		}

		if ((entity == null) || (entity.getNext() == null))
		{
			if (!useCaseProcessor.process(
				message.endUc,
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(new CollectFinalMessage<>(message.elems)),
				null
			))
			{
				var logMessage = "Unknown usecase: " + message.endUc;
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, logMessage);
				}
			}
		}
		else
		{
			entityService.send(
				ListUseCase.col.name(),
				entity.getNext(),
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(message)
			);
		}
	}
}