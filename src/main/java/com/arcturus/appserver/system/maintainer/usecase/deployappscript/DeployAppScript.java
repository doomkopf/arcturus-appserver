package com.arcturus.appserver.system.maintainer.usecase.deployappscript;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.AppManager;
import com.arcturus.appserver.system.app.type.js.script.ScriptValidator;
import com.arcturus.appserver.system.maintainer.AppScriptStore;
import com.arcturus.appserver.system.maintainer.MaintenanceUseCaseHandler;
import com.arcturus.appserver.system.maintainer.entity.AppScriptEntity;

import java.io.IOException;
import java.util.UUID;

public class DeployAppScript implements MaintenanceUseCaseHandler<DeployAppScriptRequest>
{
	private final Logger log;
	private final AppManager appManager;
	private final AppScriptStore appScriptStore;
	private final ScriptValidator scriptValidator;
	private final JsonStringSerializer jsonStringSerializer;

	public DeployAppScript(
		LoggerFactory loggerFactory,
		AppManager appManager,
		AppScriptStore appScriptStore,
		ScriptValidator scriptValidator,
		JsonStringSerializer jsonStringSerializer
	)
	{
		log = loggerFactory.create(getClass());
		this.appManager = appManager;
		this.appScriptStore = appScriptStore;
		this.scriptValidator = scriptValidator;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public Class<DeployAppScriptRequest> getRequestType()
	{
		return DeployAppScriptRequest.class;
	}

	@Override
	public void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		DeployAppScriptRequest request
	)
	{
		if (userId == null)
		{
			requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
			return;
		}

		String script;
		try
		{
			script = CryptTools.base64Unzip(request.getScript());
		}
		catch (IOException ioe)
		{
			requestContext.respond(jsonStringSerializer.toJsonString(new DeployAppScriptResponse(NetStatusCode.invalidRequest,
				"Error unzipping script"
			)));
			log.log(LogLevel.error, ioe);
			return;
		}

		scriptValidator.validate(script, result ->
		{
			if (!result.ok)
			{
				requestContext.respond(jsonStringSerializer.toJsonString(new DeployAppScriptResponse(NetStatusCode.invalidRequest,
					result.message
				)));
				return;
			}

			try
			{
				appManager.shutdownApp(request.getAppId(), null);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}

			appScriptStore.getAppScript(request.getAppId(), appScript ->
			{
				if (appScript != null)
				{
					if (!appScript.getMaintainerUserId().equals(userId))
					{
						requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
						return;
					}
					appScript.setScript(script);
				}
				else
				{
					appScript = new AppScriptEntity(script, userId);
				}

				appScriptStore.storeAppScript(request.getAppId(), appScript, () ->
				{
					appManager.disableMaintenance(request.getAppId(), true);
					requestContext.respond(NetCodes.OK_JSON);
				});
			});
		});
	}
}