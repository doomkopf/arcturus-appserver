package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.ArcturusResponseSender;

public class JavaResponseSender implements ResponseSender
{
	private final ArcturusResponseSender responseSender;
	private final JsonStringSerializer jsonStringSerializer;

	public JavaResponseSender(
			ArcturusResponseSender responseSender,
			JsonStringSerializer jsonStringSerializer)
	{
		this.responseSender = responseSender;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public void sendString(long requestId, String payload)
	{
		responseSender.send(requestId, payload);
	}

	@Override
	public void sendObject(long requestId, Object payload)
	{
		sendString(requestId, jsonStringSerializer.toJsonString(payload));
	}
}