package com.arcturus.appserver.system.account.entity;

public class UserIdToToken
{
	private String token;

	private UserIdToToken()
	{
	}

	public UserIdToToken(String token)
	{
		this.token = token;
	}

	public String getToken()
	{
		return token;
	}
}