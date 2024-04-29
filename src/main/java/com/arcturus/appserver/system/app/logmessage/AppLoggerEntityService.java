package com.arcturus.appserver.system.app.logmessage;

import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.list.AutoListElementTypeSerializer;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.ListChunkEntityServiceFactory;
import com.arcturus.appserver.system.app.service.entity.list.ListChunkUseCase;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements.CollectElementsMessage;
import com.google.gson.reflect.TypeToken;

import java.net.UnknownHostException;
import java.util.Collections;

public class AppLoggerEntityService
{
	private final ArcturusEntityService entityService;
	private final ListService<LogMessage> listService;

	public AppLoggerEntityService(
		ListChunkEntityServiceFactory listChunkEntityServiceFactory,
		JsonStringSerializer jsonStringSerializer,
		ArcturusUserSender userSender
	) throws UnknownHostException
	{
		var result = listChunkEntityServiceFactory.create("log",
			new TypeToken<ListChunk<LogMessage>>()
			{
			}.getType(),
			new TypeToken<CollectElementsMessage<LogMessage>>()
			{
			}.getType(),
			new AutoListElementTypeSerializer<>(LogMessage.class, jsonStringSerializer),
			Collections.singletonList(new ListChunkUseCase<>(LogMessageUseCase.ol.name(),
				new OpenAppLog(jsonStringSerializer, userSender)
			))
		);

		entityService = result.entityService;
		listService = result.listService;
	}

	public ArcturusEntityService getEntityService()
	{
		return entityService;
	}

	public ListService<LogMessage> getListService()
	{
		return listService;
	}

	public void shutdown() throws InterruptedException
	{
		entityService.shutdown();
	}
}