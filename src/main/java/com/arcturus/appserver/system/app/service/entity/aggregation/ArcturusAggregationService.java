package com.arcturus.appserver.system.app.service.entity.aggregation;

import com.arcturus.api.service.entity.aggregation.AggregationIndex;
import com.arcturus.api.service.entity.aggregation.AggregationService;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.CollectEntityIdsForAggregation.Message;
import com.arcturus.appserver.system.app.service.entity.list.ArcturusListServiceProvider;
import com.arcturus.appserver.system.app.service.entity.list.ListUseCase;

import java.util.Collection;
import java.util.UUID;

public class ArcturusAggregationService implements AggregationService
{
	private final ArcturusListServiceProvider listServiceProvider;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusAggregationService(
		ArcturusListServiceProvider listServiceProvider, JsonStringSerializer jsonStringSerializer
	)
	{
		this.listServiceProvider = listServiceProvider;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public void start(
		String entityServiceName,
		String mappingUseCaseId,
		String finalUseCaseId,
		Collection<AggregationIndex> aggregationIndices,
		long requestId,
		UUID requestingUserId
	)
	{
		var aggregationContextId = UUID.randomUUID();
		var messagePayload = jsonStringSerializer.toJsonString(new Message(
			mappingUseCaseId,
			finalUseCaseId,
			entityServiceName,
			aggregationContextId,
			aggregationIndices.size()
		));
		for (var aggregationIndex : aggregationIndices)
		{
			listServiceProvider.getInternalServiceByName(aggregationIndex.listServiceName)
				.getEntityService()
				.send(
					ListUseCase.cola.name(),
					aggregationIndex.indexEntityId,
					requestId,
					requestingUserId,
					messagePayload
				);
		}
	}
}