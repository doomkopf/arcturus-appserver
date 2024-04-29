package com.arcturus.appserver.system.app.service.entity.aggregation.usecase;

import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationUseCase;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.CollectEntityIdsForAggregation.Message;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.ListUseCase;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class CollectEntityIdsForAggregation<T> // working around broken java generics...
	extends PojoPayloadEntityUseCaseHandler<ListChunk<UUID>, Message>
{
	public static class Message
	{
		String mapUc;
		String endUc;
		UUID aggCtx;
		int indices;
		String esn;
		Collection<UUID> ids;

		private Message()
		{
		}

		public Message(
			String mapUc,
			String endUc,
			String entityServiceName,
			UUID aggregationContextid,
			int indices
		)
		{
			this.mapUc = mapUc;
			this.endUc = endUc;
			aggCtx = aggregationContextid;
			this.indices = indices;
			esn = entityServiceName;
			ids = new LinkedList<>();
		}
	}

	private final JsonStringSerializer jsonStringSerializer;
	private final ArcturusEntityService entityService;
	private final ArcturusEntityService aggregationService;

	public CollectEntityIdsForAggregation(
		JsonStringSerializer jsonStringSerializer,
		ArcturusEntityService entityService,
		ArcturusEntityService aggregationService
	)
	{
		super(jsonStringSerializer);
		this.jsonStringSerializer = jsonStringSerializer;
		this.entityService = entityService;
		this.aggregationService = aggregationService;
	}

	@Override
	protected Type getPayloadType()
	{
		return Message.class;
	}

	@Override
	protected void handle(
		ListChunk<UUID> entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Message message,
		UseCaseContext context
	)
	{
		if (entity != null)
		{
			for (var elem : entity)
			{
				message.ids.add(elem);
			}
		}

		if ((entity == null) || (entity.getNext() == null))
		{
			aggregationService.send(AggregationUseCase.agg.name(),
				message.aggCtx,
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(new AggregateEntityIds.Message(message.mapUc,
					message.endUc,
					message.indices,
					message.esn,
					message.ids
				))
			);
		}
		else
		{
			entityService.send(ListUseCase.cola.name(),
				entity.getNext(),
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(message)
			);
		}
	}
}