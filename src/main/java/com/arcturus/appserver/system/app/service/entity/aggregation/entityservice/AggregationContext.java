package com.arcturus.appserver.system.app.service.entity.aggregation.entityservice;

import com.arcturus.api.service.entity.list.CollectFinalMessage;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.AggregateEntity;

import java.util.*;

public class AggregationContext
{
	private String finalUseCaseId = null;

	private int pendingIndices = -1;
	private Collection<UUID> ids = new LinkedList<>();

	private int pendingEntityAggregates = -1;
	private List<String> entityJsonAggregates = null;

	public void aggregateEntityIds(
		UUID aggregationContextId,
		String mappingUseCaseId,
		String finalUseCaseId,
		long requestId,
		UUID requestingUserId,
		int indices,
		Collection<UUID> idsToAdd,
		JsonStringSerializer jsonStringSerializer,
		ArcturusEntityService entityService,
		UseCaseProcessor useCaseProcessor
	)
	{
		this.finalUseCaseId = finalUseCaseId;

		if (pendingIndices == -1)
		{
			pendingIndices = indices;
			ids.addAll(idsToAdd);
		}
		else
		{
			ids.retainAll(idsToAdd);
		}

		if (--pendingIndices <= 0)
		{
			pendingEntityAggregates = ids.size();
			if (pendingEntityAggregates == 0)
			{
				useCaseProcessor.process(finalUseCaseId,
					requestId,
					requestingUserId,
					CollectFinalMessage.JSON_EMPTY,
					null
				);
				return;
			}

			entityJsonAggregates = new ArrayList<>(pendingEntityAggregates);

			for (var entityId : ids)
			{
				entityService.send(InternalUseCases.AGGREGATE_ENTITY,
					entityId,
					requestId,
					requestingUserId,
					jsonStringSerializer.toJsonString(new AggregateEntity.Message(
						aggregationContextId,
						mappingUseCaseId
					))
				);
			}

			ids = null;
		}
	}

	public void finishedEntityAggregation(
		String entityJsonAggregate,
		UseCaseProcessor useCaseProcessor,
		long requestId,
		UUID requestingUserId
	)
	{
		entityJsonAggregates.add(entityJsonAggregate);

		if (--pendingEntityAggregates == 0)
		{
			useCaseProcessor.process(finalUseCaseId,
				requestId,
				requestingUserId,
				CollectFinalMessage.toJsonWithStringSerializedJsonElems(entityJsonAggregates),
				null
			);
		}
	}
}