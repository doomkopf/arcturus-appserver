package com.arcturus.appserver.system.app.service.entity.list;

import java.util.List;
import java.util.UUID;

public interface InternalListChunkService<T>
{
	void remove(T elemToRemove, UUID entityId);

	void transferList(UUID toEntityId, UUID next, List<T> list);
}