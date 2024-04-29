package com.arcturus.appserver.system.app.service.entity.aggregation.usecase;

import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationContext;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.AggregateEntityIds.Message;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.UUID;

public class AggregateEntityIds extends PojoPayloadEntityUseCaseHandler<AggregationContext, Message>
{
	public static class Message
	{
		String mapUc;
		String endUc;
		int indices;
		String esn;
		Collection<UUID> ids;

		private Message()
		{
		}

		public Message(
			String mappingUseCaseId,
			String endUc,
			int indices,
			String entityServiceName,
			Collection<UUID> ids
		)
		{
			mapUc = mappingUseCaseId;
			this.endUc = endUc;
			this.indices = indices;
			esn = entityServiceName;
			this.ids = ids;
		}
	}

	private final JsonStringSerializer jsonStringSerializer;
	private final UserEntityServiceProvider serviceProvider;
	private final UseCaseProcessor useCaseProcessor;

	public AggregateEntityIds(
		JsonStringSerializer jsonStringSerializer,
		UserEntityServiceProvider serviceProvider,
		UseCaseProcessor useCaseProcessor
	)
	{
		super(jsonStringSerializer);
		this.jsonStringSerializer = jsonStringSerializer;
		this.serviceProvider = serviceProvider;
		this.useCaseProcessor = useCaseProcessor;
	}

	@Override
	protected Type getPayloadType()
	{
		return Message.class;
	}

	@Override
	protected void handle(
		AggregationContext aggregationContext,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Message message,
		UseCaseContext useCaseContext
	)
	{
		aggregationContext.aggregateEntityIds(
			id,
			message.mapUc,
			message.endUc,
			requestId,
			requestingUserId,
			message.indices,
			message.ids,
			jsonStringSerializer,
			serviceProvider.getServiceByName(message.esn),
			useCaseProcessor
		);
	}
}