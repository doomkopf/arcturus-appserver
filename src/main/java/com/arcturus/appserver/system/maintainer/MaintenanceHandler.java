package com.arcturus.appserver.system.maintainer;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.AppManager;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.SessionIdGenerator;
import com.arcturus.appserver.system.UserSessionContainer;
import com.arcturus.appserver.system.app.type.js.script.ScriptValidator;
import com.arcturus.appserver.system.maintainer.usecase.deployappscript.DeployAppScript;
import com.arcturus.appserver.system.maintainer.usecase.getappscript.GetAppScript;
import com.arcturus.appserver.system.maintainer.usecase.getswaggerinfo.GetSwaggerInfo;
import com.arcturus.appserver.system.maintainer.usecase.login.MaintainerLogin;
import com.arcturus.appserver.system.maintainer.usecase.register.MaintainerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaintenanceHandler
{
	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final UserSessionContainer userSessionContainer;

	private final Map<String, MaintenanceUseCaseHandler<?>> maintenanceUseCaseHandlers = new HashMap<>();

	public MaintenanceHandler(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer,
		AppManager appManager,
		AppScriptStore appScriptStore,
		SessionIdGenerator sessionIdGenerator,
		UserSessionContainer userSessionContainer,
		ScriptValidator scriptValidator
	)
	{
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.userSessionContainer = userSessionContainer;

		maintenanceUseCaseHandlers.put(InternalUseCases.MAINTAINER_LOGIN,
			new MaintainerLogin(db, jsonStringSerializer, sessionIdGenerator, userSessionContainer)
		);
		maintenanceUseCaseHandlers.put(InternalUseCases.MAINTAINER_REGISTRATION,
			new MaintainerRegistration(loggerFactory, db, jsonStringSerializer)
		);
		maintenanceUseCaseHandlers.put(InternalUseCases.DEPLOY_APPSCRIPT,
			new DeployAppScript(loggerFactory,
				appManager,
				appScriptStore,
				scriptValidator,
				jsonStringSerializer
			)
		);
		maintenanceUseCaseHandlers.put(InternalUseCases.GET_APPSCRIPT,
			new GetAppScript(loggerFactory, jsonStringSerializer, appScriptStore)
		);
		maintenanceUseCaseHandlers.put(InternalUseCases.GET_SWAGGER_INFO,
			new GetSwaggerInfo(appManager)
		);
	}

	public boolean handleMaintenanceUseCases(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String headerAppId,
		String useCaseId,
		Long sessionId,
		String jsonPayload
	)
	{
		var localAppId = headerAppId;
		UUID userId = null;
		if (persistentLocalSession != null)
		{
			var sessionInfo = persistentLocalSession.getInfo();
			if (sessionInfo != null)
			{
				localAppId = sessionInfo.getAppId();
				userId = sessionInfo.getUserId();
			}
		}

		var appId = localAppId;

		var useCaseHandler = maintenanceUseCaseHandlers.get(useCaseId);
		if (useCaseHandler == null)
		{
			return false;
		}

		if (userId != null)
		{
			handleUseCase(useCaseHandler,
				requestContext,
				persistentLocalSession,
				appId,
				userId,
				jsonPayload
			);
			return true;
		}

		if (sessionId == null)
		{
			handleUseCase(useCaseHandler,
				requestContext,
				persistentLocalSession,
				appId,
				null,
				jsonPayload
			);
			return true;
		}

		userSessionContainer.getUserIdBySessionId(sessionId, localUserId -> handleUseCase(
			useCaseHandler,
			requestContext,
			persistentLocalSession,
			appId,
			localUserId,
			jsonPayload
		));

		return true;
	}

	@SuppressWarnings("unchecked")
	private void handleUseCase(
		@SuppressWarnings("rawtypes")
			MaintenanceUseCaseHandler useCaseHandler,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		String jsonPayload
	)
	{
		try
		{
			useCaseHandler.handle(requestContext,
				persistentLocalSession,
				appId,
				userId,
				jsonStringSerializer.fromJsonString(useCaseHandler.getRequestType(), jsonPayload)
			);
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}
}
