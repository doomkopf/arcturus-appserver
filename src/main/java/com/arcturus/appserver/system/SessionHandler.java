package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.net.*;
import com.arcturus.appserver.system.app.App;
import com.arcturus.appserver.system.maintainer.MaintenanceHandler;
import com.arcturus.appserver.system.message.Request;

/**
 * Handling all connection (connect, disconnect, reception) based events from
 * the clients/sessions.
 *
 * @author doomkopf
 */
public class SessionHandler implements PersistentLocalSessionListener, HttpSessionListener
{
	private final Logger log;
	private final AppManager appManager;
	private final DdosManager ddosManager;
	private final UserSessionContainer userSessionContainer;
	private final MaintenanceHandler maintenanceHandler;

	public SessionHandler( // NOSONAR
		LoggerFactory loggerFactory,
		AppManager appManager,
		PersistentLocalSessionService persistentLocalSessionService,
		HttpSessionService httpSessionService,
		DdosManager ddosManager,
		UserSessionContainer userSessionContainer,
		MaintenanceHandler maintenanceHandler
	)
	{
		log = loggerFactory.create(getClass());
		this.appManager = appManager;
		this.ddosManager = ddosManager;
		this.userSessionContainer = userSessionContainer;
		this.maintenanceHandler = maintenanceHandler;

		persistentLocalSessionService.registerSessionListener(this);
		httpSessionService.registerSessionListener(this);
	}

	private void logPayload(HttpMethod method, String path, String queryString, String payload)
	{
		if (log.isLogLevel(LogLevel.debug))
		{
			if (method != null)
			{
				log.log(LogLevel.debug, "Received method: " + method);
			}

			if (path != null)
			{
				log.log(LogLevel.debug, "Received path: " + path);
			}

			if (queryString != null)
			{
				log.log(LogLevel.debug, "Received queryString: " + queryString);
			}

			log.log(LogLevel.debug, "Received payload: " + payload);
		}
	}

	@Override
	public void onConnected(PersistentLocalSession localSession)
	{
	}

	@Override
	public void onDisconnected(PersistentLocalSession localSession)
	{
		var persistentLocalSessionInfo = localSession.getInfo();
		if (persistentLocalSessionInfo == null)
		{
			return;
		}

		var app = appManager.getApp(persistentLocalSessionInfo.getAppId());
		if (app == null)
		{
			return;
		}

		// TODO the session didn't really end yet as long as the sessionId is
		// still valid. Just temp for the current project using it
		try
		{
			if (app.getUserSessionHandler() != null)
			{
				app.getUserSessionHandler().onSessionEnded(persistentLocalSessionInfo.getUserId());
			}
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, e);
			}
		}

		userSessionContainer.removePersistentLocalSessionByUserId(persistentLocalSessionInfo.getUserId());
	}

	@Override
	public void onReceived(PersistentLocalSession localSession, String payload)
	{
		logPayload(null, null, null, payload);

		handleRequest(localSession, localSession, Request.parseFromPayload(payload));
	}

	@Override
	public void onReceived(RequestContext requestContext, String payload)
	{
		logPayload(null, null, null, payload);

		handleRequest(requestContext, null, Request.parseFromPayload(payload));
	}

	@Override
	public void onReceived(
		RequestContext requestContext,
		HttpHeaders httpHeaders,
		HttpMethod method,
		String path,
		String queryString,
		String requestBody
	)
	{
		logPayload(method, path, queryString, requestBody);

		handleRequest(
			requestContext,
			null,
			Request.parseFromPathAndRequestBody(path, queryString, requestBody)
		);
	}

	private void handleRequest(
		RequestContext requestContext, PersistentLocalSession localSession, Request request
	)
	{
		if ((localSession != null) && ddosManager.checkAndHandleDDOS(localSession))
		{
			return;
		}

		if (request == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Invalid request");
			}
			requestContext.respond(NetCodes.ERROR_JSON_INVALID_REQUEST);
			return;
		}

		var requestHeader = request.getRequestHeader();

		var useCaseId = requestHeader.getUseCaseId();
		if (useCaseId == null)
		{
			requestContext.respond(NetCodes.ERROR_JSON_INVALID_USECASE);
			return;
		}

		if (maintenanceHandler.handleMaintenanceUseCases(
			requestContext,
			localSession,
			requestHeader.getAppId(),
			useCaseId,
			requestHeader.getSessionId(),
			request.getBody()
		))
		{
			return;
		}

		var appId = requestHeader.getAppId();
		if (appId == null)
		{
			if (localSession != null)
			{
				var persistentLocalSessionInfo = localSession.getInfo();
				if (persistentLocalSessionInfo == null)
				{
					requestContext.respond(NetCodes.ERROR_JSON_INVALID_APP_ID);
					return;
				}

				appId = persistentLocalSessionInfo.getAppId();
				if (appId == null)
				{
					requestContext.respond(NetCodes.ERROR_JSON_INVALID_APP_ID);
					return;
				}
			}
			else
			{
				requestContext.respond(NetCodes.ERROR_JSON_INVALID_APP_ID);
				return;
			}
		}

		var app = handleAppId(requestContext, appId);
		if (app == null)
		{
			return;
		}

		if (app.getPrivateUseCases().isPrivate(requestHeader.getService(), useCaseId))
		{
			requestContext.respond(NetCodes.ERROR_JSON_INVALID_USECASE);
			return;
		}

		var sessionId = requestHeader.getSessionId();
		if (sessionId != null)
		{
			userSessionContainer.getUserIdBySessionId(sessionId, userId ->
			{
				if (userId == null)
				{
					requestContext.respond(NetCodes.ERROR_JSON_INVALID_SESSION_ID);
					return;
				}

				userSessionContainer.putRequestContext(userId, requestContext);
				app.handleUseCase(
					requestContext,
					localSession,
					useCaseId,
					requestHeader.getService(),
					requestHeader.getEntityId(),
					sessionId,
					request.getBody()
				);
			});
			return;
		}

		app.handleUseCase(
			requestContext,
			localSession,
			useCaseId,
			requestHeader.getService(),
			requestHeader.getEntityId(),
			null,
			request.getBody()
		);
	}

	private App handleAppId(RequestContext requestContext, String appId)
	{
		var app = appManager.getApp(appId);
		if (app == null)
		{
			if (appManager.isAppUnderMaintenance(appId))
			{
				requestContext.respond(NetCodes.ERROR_JSON_APP_UNDER_MAINTENANCE);
			}
			else
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, App.MSG_APP_NOT_FOUND + appId);
				}
				requestContext.respond(NetCodes.ERROR_JSON_INVALID_APP_ID);
			}
		}

		return app;
	}
}