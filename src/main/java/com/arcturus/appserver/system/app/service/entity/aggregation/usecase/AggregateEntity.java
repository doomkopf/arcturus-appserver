package com.arcturus.appserver.system.app.service.entity.aggregation.usecase;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationUseCase;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.AggregateEntity.Message;
import com.arcturus.appserver.system.app.service.entity.mapping.MappingEntityUseCaseProvider;

import java.lang.reflect.Type;
import java.util.UUID;

public class AggregateEntity<E> extends PojoPayloadEntityUseCaseHandler<E, Message>
{
	public static class Message
	{
		UUID aggCtx;
		String uc;

		private Message()
		{
		}

		public Message(UUID aggregationContextId, String mappingUseCaseId)
		{
			aggCtx = aggregationContextId;
			uc = mappingUseCaseId;
		}
	}

	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final ArcturusEntityService aggregationService;
	private final MappingEntityUseCaseProvider<E> mappingEntityUseCaseHandlerProvider;
	private final ArcturusResponseSender responseSender;

	public AggregateEntity(
		LoggerFactory loggerFactory,
		JsonStringSerializer jsonStringSerializer,
		ArcturusEntityService aggregationService,
		MappingEntityUseCaseProvider<E> mappingEntityUseCaseHandlerProvider,
		ArcturusResponseSender responseSender
	)
	{
		super(jsonStringSerializer);
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.aggregationService = aggregationService;
		this.mappingEntityUseCaseHandlerProvider = mappingEntityUseCaseHandlerProvider;
		this.responseSender = responseSender;
	}

	@Override
	protected Type getPayloadType()
	{
		return Message.class;
	}

	@Override
	protected void handle(
		E entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Message message,
		UseCaseContext context
	) throws ArcturusAppException
	{
		var mapper = mappingEntityUseCaseHandlerProvider.get(message.uc);
		if (mapper == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Unknown mapping usecase: " + message.uc);
			}
			responseSender.send(requestId, NetCodes.ERROR_JSON_INVALID_USECASE);
			return;
		}

		var aggregate = mapper.map(entity, id);

		aggregationService.send(
			AggregationUseCase.fin.name(),
			message.aggCtx,
			requestId,
			requestingUserId,
			jsonStringSerializer.toJsonString(new HandleFinishedAggregations.Message(aggregate))
		);
	}
}