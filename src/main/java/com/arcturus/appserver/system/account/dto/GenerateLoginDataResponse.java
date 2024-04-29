package com.arcturus.appserver.system.account.dto;

import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.InternalUseCases;

/**
 * DTO for the response containing the generated login data.
 *
 * @author doomkopf
 */
public class GenerateLoginDataResponse
{
	String uc = InternalUseCases.GENERATE_LOGIN_DATA;
	NetStatusCode status = NetStatusCode.ok;
	String user;
	String password;

	public GenerateLoginDataResponse(String user, String password)
	{
		this.user = user;
		this.password = password;
	}
}