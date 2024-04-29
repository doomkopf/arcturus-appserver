package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.api.service.entity.EntityUseCaseHandler;

public class ListChunkUseCase<T>
{
	public final String useCaseId;
	public final EntityUseCaseHandler<ListChunk<T>> useCaseHandler;

	public ListChunkUseCase(
		String useCaseId, EntityUseCaseHandler<ListChunk<T>> useCaseHandler
	)
	{
		this.useCaseId = useCaseId;
		this.useCaseHandler = useCaseHandler;
	}
}