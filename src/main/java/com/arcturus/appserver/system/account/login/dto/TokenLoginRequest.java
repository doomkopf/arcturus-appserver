package com.arcturus.appserver.system.account.login.dto;

public class TokenLoginRequest
{
	private String token;

	private TokenLoginRequest()
	{
	}

	public TokenLoginRequest(String token)
	{
		this.token = token;
	}

	public String getToken()
	{
		return token;
	}
}