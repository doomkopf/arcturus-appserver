package com.arcturus.appserver.net;

import java.util.UUID;

public class PersistentLocalSessionInfo
{
	private final UUID userId;
	private final String appId;

	public PersistentLocalSessionInfo(UUID userId, String appId)
	{
		this.userId = userId;
		this.appId = appId;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public String getAppId()
	{
		return appId;
	}
}