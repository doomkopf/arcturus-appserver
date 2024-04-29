package com.arcturus.appserver.system.account.entity;

import com.arcturus.appserver.system.account.AccountLoginType;

import java.util.UUID;

public class LoginToken
{
	private UUID userId;
	private AccountLoginType loginType;
	private long expiresAt;

	private LoginToken()
	{
	}

	public LoginToken(UUID userId, AccountLoginType loginType, long expiresAtTimestampMillis)
	{
		this.userId = userId;
		this.loginType = loginType;
		expiresAt = expiresAtTimestampMillis;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public AccountLoginType getLoginType()
	{
		return loginType;
	}

	public boolean isExpired()
	{
		return System.currentTimeMillis() > expiresAt;
	}
}