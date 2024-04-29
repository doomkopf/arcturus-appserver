package com.arcturus.appserver.system.app.type.java;

import java.util.UUID;

import com.arcturus.api.UserSender;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.ArcturusUserSender;

public class JavaUserSender implements UserSender
{
	private final ArcturusUserSender userSender;
	private final JsonStringSerializer jsonStringSerializer;

	public JavaUserSender(ArcturusUserSender userSender, JsonStringSerializer jsonStringSerializer)
	{
		this.userSender = userSender;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public void sendString(UUID userId, String payload)
	{
		userSender.send(userId, payload);
	}

	@Override
	public void sendObject(UUID userId, Object payload)
	{
		sendString(userId, jsonStringSerializer.toJsonString(payload));
	}
}