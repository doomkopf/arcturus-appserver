package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements.CollectElementsMessage;

import java.util.List;
import java.util.UUID;

public class ArcturusListChunkService<T> implements ListService<T>, InternalListChunkService<T>
{
	private final ArcturusEntityService service;
	private final JsonStringSerializer jsonStringSerializer;
	private final ListElementTypeSerializer<T> serializer;
	private final JsonFactory jsonFactory;

	public ArcturusListChunkService(
		ArcturusEntityService service,
		JsonStringSerializer jsonStringSerializer,
		ListElementTypeSerializer<T> serializer,
		JsonFactory jsonFactory
	)
	{
		this.service = service;
		this.jsonStringSerializer = jsonStringSerializer;
		this.serializer = serializer;
		this.jsonFactory = jsonFactory;
	}

	public ArcturusEntityService getEntityService()
	{
		return service;
	}

	@Override
	public void add(T elemToAdd, UUID entityId)
	{
		service.send(ListUseCase.add.name(),
			entityId,
			0,
			null,
			serializer.elementToString(elemToAdd)
		);
	}

	@Override
	public void remove(T elemToRemove, UUID entityId)
	{
		service.send(ListUseCase.rem.name(),
			entityId,
			0,
			null,
			serializer.elementToString(elemToRemove)
		);
	}

	@Override
	public void collect(String finalUseCaseId, UUID entityId, long requestId, UUID requestingUserId)
	{
		service.send(ListUseCase.col.name(),
			entityId,
			requestId,
			requestingUserId,
			jsonStringSerializer.toJsonString(new CollectElementsMessage<>(finalUseCaseId))
		);
	}

	@Override
	public void transferList(UUID toEntityId, UUID next, List<T> list)
	{
		var json = jsonFactory.create();

		if (next != null)
		{
			json.setString("n", next.toString());
		}

		var jsonList = json.createArray("l");
		for (var elem : list)
		{
			jsonList.addString(serializer.elementToString(elem));
		}

		service.send(ListUseCase.tli.name(), toEntityId, 0, null, json.toString());
	}
}