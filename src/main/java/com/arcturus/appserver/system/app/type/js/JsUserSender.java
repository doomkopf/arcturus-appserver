package com.arcturus.appserver.system.app.type.js;

import com.arcturus.appserver.system.ArcturusUserSender;

import java.util.UUID;

public class JsUserSender
{
	private final ArcturusUserSender userSender;

	public JsUserSender(ArcturusUserSender userSender)
	{
		this.userSender = userSender;
	}

	public void send(String userId, String payload)
	{
		userSender.send((userId == null) ? null : UUID.fromString(userId), payload);
	}
}