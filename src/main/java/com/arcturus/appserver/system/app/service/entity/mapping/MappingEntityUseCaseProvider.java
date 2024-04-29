package com.arcturus.appserver.system.app.service.entity.mapping;

import com.arcturus.api.service.entity.aggregation.MappingEntityUseCaseHandler;

import java.util.Map;

public class MappingEntityUseCaseProvider<E>
{
	private final Map<String, MappingEntityUseCaseHandler<E>> mappingEntityUseCaseHandlerMap;

	public MappingEntityUseCaseProvider(Map<String, MappingEntityUseCaseHandler<E>> mappingEntityUseCaseHandlerMap)
	{
		this.mappingEntityUseCaseHandlerMap = mappingEntityUseCaseHandlerMap;
	}

	public MappingEntityUseCaseHandler<E> get(String useCaseId)
	{
		return mappingEntityUseCaseHandlerMap.get(useCaseId);
	}
}