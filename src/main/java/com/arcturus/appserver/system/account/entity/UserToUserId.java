package com.arcturus.appserver.system.account.entity;

import java.util.UUID;

/**
 * A mapping entity from user to userId.
 * 
 * @author doomkopf
 */
public class UserToUserId
{
	private UUID userId;

	@SuppressWarnings("unused")
	private UserToUserId()
	{
	}

	public UserToUserId(UUID userId)
	{
		this.userId = userId;
	}

	public UUID getUserId()
	{
		return userId;
	}
}