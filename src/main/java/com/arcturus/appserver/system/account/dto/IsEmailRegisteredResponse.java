package com.arcturus.appserver.system.account.dto;

import com.arcturus.appserver.system.InternalUseCases;

public class IsEmailRegisteredResponse
{
	final String uc = InternalUseCases.IS_EMAIL_REGISTERED;
	boolean isRegistered;

	public IsEmailRegisteredResponse(boolean isRegistered)
	{
		this.isRegistered = isRegistered;
	}
}