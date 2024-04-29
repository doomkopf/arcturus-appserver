package com.arcturus.appserver.system.app.service.entity.list.usecase;

import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.appserver.system.app.service.entity.list.InternalListChunkService;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.ListElementTypeSerializer;

import java.util.UUID;

public class AddElement<T> implements EntityUseCaseHandler<ListChunk<T>>
{
	private final InternalListChunkService<T> listChunkService;
	private final ListElementTypeSerializer<T> serializer;

	public AddElement(
		InternalListChunkService<T> listChunkService, ListElementTypeSerializer<T> serializer
	)
	{
		this.listChunkService = listChunkService;
		this.serializer = serializer;
	}

	@Override
	public void handle(
		ListChunk<T> entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		String payload,
		UseCaseContext context
	)
	{
		entity.add(serializer.elementFromString(payload), listChunkService);
		context.dirty();
	}
}