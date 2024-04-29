package com.arcturus.appserver.system.maintainer.usecase.getappscript;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.maintainer.AppScriptStore;
import com.arcturus.appserver.system.maintainer.MaintenanceUseCaseHandler;

import java.io.IOException;
import java.util.UUID;

public class GetAppScript implements MaintenanceUseCaseHandler<GetAppScriptRequest>
{
	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final AppScriptStore appScriptStore;

	public GetAppScript(
		LoggerFactory loggerFactory,
		JsonStringSerializer jsonStringSerializer,
		AppScriptStore appScriptStore
	)
	{
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.appScriptStore = appScriptStore;
	}

	@Override
	public Class<GetAppScriptRequest> getRequestType()
	{
		return GetAppScriptRequest.class;
	}

	@Override
	public void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		GetAppScriptRequest request
	)
	{
		if (userId == null)
		{
			requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
			return;
		}

		appScriptStore.getAppScript(request.getAppId(), script ->
		{
			if (script == null)
			{
				try
				{
					requestContext.respond(jsonStringSerializer.toJsonString(new GetAppScriptResponse(request.getAppId(),
						CryptTools.base64Zip("")
					)));
				}
				catch (IOException e)
				{
					log.log(LogLevel.error, e);
					requestContext.respond(NetCodes.ERROR_JSON_INTERNAL_ERROR);
				}
				return;
			}

			if (!script.getMaintainerUserId().equals(userId))
			{
				requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
				return;
			}

			try
			{
				requestContext.respond(jsonStringSerializer.toJsonString(new GetAppScriptResponse(request.getAppId(),
					CryptTools.base64Zip(script.getScript())
				)));
			}
			catch (IOException e)
			{
				log.log(LogLevel.error, e);
				requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
			}
		});
	}
}