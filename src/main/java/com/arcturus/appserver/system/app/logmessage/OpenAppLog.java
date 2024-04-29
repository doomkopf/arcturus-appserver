package com.arcturus.appserver.system.app.logmessage;

import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.app.logmessage.OpenAppLog.Request;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.internalapp.maintainer.usecase.openapplog.OpenAppLogResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

public class OpenAppLog extends PojoPayloadEntityUseCaseHandler<ListChunk<LogMessage>, Request>
{
	public static class Request
	{
	}

	private final JsonStringSerializer jsonStringSerializer;
	private final ArcturusUserSender userSender;

	OpenAppLog(
		JsonStringSerializer jsonStringSerializer, ArcturusUserSender userSender
	)
	{
		super(jsonStringSerializer);
		this.jsonStringSerializer = jsonStringSerializer;
		this.userSender = userSender;
	}

	@Override
	protected Class<Request> getPayloadType()
	{
		return Request.class;
	}

	@Override
	protected void handle(
		ListChunk<LogMessage> logMessages,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Request payload,
		UseCaseContext context
	)
	{
		if (logMessages == null)
		{
			userSender.send(requestingUserId,
				jsonStringSerializer.toJsonString(new OpenAppLogResponse(NetStatusCode.ok,
					Collections.emptyList()
				))
			);
			return;
		}

		var list = new LinkedList<LogMessage>();
		for (var logMessage : logMessages)
		{
			list.add(logMessage);
		}

		userSender.send(requestingUserId,
			jsonStringSerializer.toJsonString(new OpenAppLogResponse(NetStatusCode.ok, list))
		);
	}
}