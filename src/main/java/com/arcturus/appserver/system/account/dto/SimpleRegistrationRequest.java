package com.arcturus.appserver.system.account.dto;

/**
 * DTO for the request of the simple user/password based registration.
 * 
 * @author doomkopf
 */
public class SimpleRegistrationRequest
{
	private String user;
	private String password;

	@SuppressWarnings("unused")
	private SimpleRegistrationRequest()
	{
	}

	public SimpleRegistrationRequest(String user, String password)
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