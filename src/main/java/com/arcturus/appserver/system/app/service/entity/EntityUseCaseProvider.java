package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;

import java.util.Map;

/**
 * Provider for getting {@link EntityUseCaseHandler}s by their respective id.
 *
 * @author doomkopf
 */
public class EntityUseCaseProvider<E>
{
	private final Map<String, EntityUseCaseHandler<E>> idToUseCaseMap;
	private final Map<String, EntityTransactionUseCaseHandler<E>> idToTransactionUseCaseMap;

	public EntityUseCaseProvider(
		Map<String, EntityUseCaseHandler<E>> idToUseCaseMap,
		Map<String, EntityTransactionUseCaseHandler<E>> idToTransactionUseCaseMap
	)
	{
		this.idToUseCaseMap = idToUseCaseMap;
		this.idToTransactionUseCaseMap = idToTransactionUseCaseMap;
	}

	public EntityUseCaseHandler<E> getUseCaseHandler(String useCaseId)
	{
		return idToUseCaseMap.get(useCaseId);
	}

	public EntityTransactionUseCaseHandler<E> getTransactionUseCaseHandler(String useCaseId)
	{
		return idToTransactionUseCaseMap.get(useCaseId);
	}
}
