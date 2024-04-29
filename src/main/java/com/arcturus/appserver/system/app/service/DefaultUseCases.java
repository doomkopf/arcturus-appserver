package com.arcturus.appserver.system.app.service;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.account.AccountLoginType;
import com.arcturus.appserver.system.account.dto.SimpleRegistrationRequest;
import com.arcturus.appserver.system.account.dto.SimpleStatusResponse;
import com.arcturus.appserver.system.account.login.dto.LoginRequest;
import com.arcturus.appserver.system.account.login.dto.LoginResponse;
import com.arcturus.appserver.system.app.service.info.ServicelessUseCaseInfo;
import com.arcturus.appserver.system.app.service.info.UseCaseInfo;

public class DefaultUseCases
{
	private final UseCaseInfo[] defaultUseCases; // NOSONAR

	public DefaultUseCases(JsonStringSerializer jsonStringSerializer)
	{
		defaultUseCases = new UseCaseInfo[] {
			new ServicelessUseCaseInfo(InternalUseCases.SIMPLE_REGISTRATION,
				true,
				"",
				jsonStringSerializer.toJsonString(new SimpleRegistrationRequest("myuser",
					"superSecretPassword"
				)),
				jsonStringSerializer.toJsonString(new SimpleStatusResponse(InternalUseCases.SIMPLE_REGISTRATION,
					NetStatusCode.ok
				))
			), new ServicelessUseCaseInfo(InternalUseCases.LOGIN,
			true,
			"",
			jsonStringSerializer.toJsonString(new LoginRequest("myuser", "superSecretPassword")),
			jsonStringSerializer.toJsonString(new LoginResponse(InternalUseCases.LOGIN,
				NetStatusCode.ok,
				"sessionId",
				"loginToken",
				Constants.ZERO_UUID,
				AccountLoginType.std
			))
		)};
	}

	public UseCaseInfo[] getDefaultUseCases()
	{
		return defaultUseCases;
	}
}