package com.arcturus.appserver.system.account.login.dto;

public class LoginRequest
{
	private String user;
	private String password;

	@SuppressWarnings("unused")
	private LoginRequest()
	{
	}

	public LoginRequest(String user, String password)
	{
		this.user = user;
		this.password = password;
	}

	public String getUser()
	{
		return user;
	}

	public String getPassword()
	{
		return password;
	}
}