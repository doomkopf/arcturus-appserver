package com.arcturus.appserver.system.account.dto;

public class PasswordResetRequest
{
	public String key;

	private PasswordResetRequest()
	{
	}

	public PasswordResetRequest(String key)
	{
		this.key = key;
	}
}
