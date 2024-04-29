package com.arcturus.appserver.system.account.login.dto;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.account.AccountLoginType;

import java.util.UUID;

public class LoginResponse
{
	public static void sendLoginErrorResponse(
		JsonStringSerializer jsonStringSerializer,
		RequestContext requestContext,
		String useCaseId,
		NetStatusCode error
	)
	{
		requestContext.respond(jsonStringSerializer.toJsonString(new LoginResponse(useCaseId,
			error
		)));
	}

	String uc;
	NetStatusCode status;
	String sId;
	String token;
	UUID userId;
	AccountLoginType loginType;

	public LoginResponse(
		String useCase,
		NetStatusCode status,
		String sId,
		String token,
		UUID userId,
		AccountLoginType loginType
	)
	{
		uc = useCase;
		this.status = status;
		this.sId = sId;
		this.token = token;
		this.userId = userId;
		this.loginType = loginType;
	}

	public LoginResponse(String useCase, NetStatusCode status)
	{
		this(useCase, status, null, null, null, null);
	}
}