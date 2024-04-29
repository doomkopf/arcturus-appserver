package com.arcturus.appserver.system.maintainer.entity;

import java.util.UUID;

public class MaintainerUserToUserId
{
	private UUID userId;

	@SuppressWarnings("unused")
	private MaintainerUserToUserId()
	{
	}

	public MaintainerUserToUserId(UUID userId)
	{
		this.userId = userId;
	}

	public UUID getUserId()
	{
		return userId;
	}
}