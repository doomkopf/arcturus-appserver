package com.arcturus.appserver.system.app;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.UserSessionHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.IdGenerator;
import com.arcturus.appserver.system.RequestsContainer;
import com.arcturus.appserver.system.UserSessionContainer;
import com.arcturus.appserver.system.account.AccountManager;
import com.arcturus.appserver.system.app.logmessage.AppLoggerEntityService;
import com.arcturus.appserver.system.app.rest.SwaggerInfo;
import com.arcturus.appserver.system.app.service.EntityServiceProvider;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;

import java.util.UUID;

/**
 * An application that can be loaded by the appserver.
 *
 * @author doomkopf
 */
public class App
{
	public static final String MSG_APP_NOT_FOUND = "App not found: ";

	private final Logger log;
	private final String appId;
	private final RequestsContainer requestsContainer;
	private final UserSessionContainer userSessionContainer;
	private final AccountManager accountManager;
	private final UserEntityServiceProvider userEntityServiceProvider;
	private final EntityServiceProvider entityServiceProvider;
	private final IdGenerator requestIdGenerator;
	private final UserSessionHandlerFactory userSessionHandlerFactory;
	private final SwaggerInfo swaggerInfo;
	private final PrivateUseCases privateUseCases;
	private final ArcturusEntityService appLoggerEntityService;
	private final UseCaseProcessor useCaseProcessor;

	private UserSessionHandler userSessionHandler;

	public App(
		LoggerFactory loggerFactory,
		String appId,
		RequestsContainer requestsContainer,
		UserSessionContainer userSessionContainer,
		AccountManager accountManager,
		UserEntityServiceProvider userEntityServiceProvider,
		EntityServiceProvider entityServiceProvider,
		IdGenerator requestIdGenerator,
		UserSessionHandlerFactory userSessionHandlerFactory,
		SwaggerInfo swaggerInfo,
		PrivateUseCases privateUseCases,
		AppLoggerEntityService appLoggerEntityService,
		UseCaseProcessor useCaseProcessor
	)
	{
		log = loggerFactory.create(getClass());
		this.appId = appId;
		this.requestsContainer = requestsContainer;
		this.userSessionContainer = userSessionContainer;
		this.accountManager = accountManager;
		this.userEntityServiceProvider = userEntityServiceProvider;
		this.entityServiceProvider = entityServiceProvider;
		this.requestIdGenerator = requestIdGenerator;
		this.userSessionHandlerFactory = userSessionHandlerFactory;
		this.swaggerInfo = swaggerInfo;
		this.privateUseCases = privateUseCases;
		this.appLoggerEntityService = appLoggerEntityService.getEntityService();
		this.useCaseProcessor = useCaseProcessor;
	}

	void init()
	{
		userSessionHandler = userSessionHandlerFactory.create();
	}

	public void handleUseCase(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String useCaseId,
		String service,
		UUID entityId,
		Long requestSessionId,
		String jsonPayload
	)
	{
		var requestId = requestIdGenerator.generate();
		requestsContainer.put(requestId, requestContext);

		if (persistentLocalSession != null)
		{
			var persistentLocalSessionInfo = persistentLocalSession.getInfo();
			if (persistentLocalSessionInfo != null)
			{
				handleUseCase(requestContext,
					persistentLocalSession,
					requestId,
					persistentLocalSessionInfo.getUserId(),
					useCaseId,
					service,
					entityId,
					jsonPayload,
					requestContext
				);
				return;
			}
		}

		if (requestSessionId == null)
		{
			handleUseCase(requestContext,
				persistentLocalSession,
				requestId,
				null,
				useCaseId,
				service,
				entityId,
				jsonPayload,
				requestContext
			);
			return;
		}

		userSessionContainer.getUserIdBySessionId(requestSessionId, userId ->
		{
			if (userId == null)
			{
				requestContext.respond(NetCodes.ERROR_JSON_INVALID_SESSION_ID);
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug,
						"Invalid sessionId - dropping message: " + requestSessionId
					);
				}
				return;
			}

			handleUseCase(requestContext,
				persistentLocalSession,
				requestId,
				userId,
				useCaseId,
				service,
				entityId,
				jsonPayload,
				requestContext
			);
		});
	}

	private void determineServiceAndSend(
		long requestId,
		UUID userId,
		String useCaseId,
		String serviceName,
		UUID entityId,
		String jsonPayload
	)
	{
		var service = userEntityServiceProvider.getServiceByName(serviceName);
		if (service == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Unknown service: " + serviceName);
			}
			return;
		}

		service.send(useCaseId, entityId, requestId, userId, jsonPayload);
	}

	private void handleUseCase(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		long requestId,
		UUID userId,
		String useCaseId,
		String service,
		UUID entityId,
		String jsonPayload,
		RequestInfo requestInfo
	)
	{
		if (accountManager.handleAccountUseCases(requestContext,
			persistentLocalSession,
			appId,
			useCaseId,
			userId,
			jsonPayload
		))
		{
			return;
		}

		if (useCaseProcessor.process(useCaseId, requestId, userId, jsonPayload, requestInfo))
		{
			return;
		}

		if ((service == null)
			|| service.isEmpty()
			|| service.equals(ArcturusEntityService.SERVICE_NAME_USER))
		{
			userEntityServiceProvider.getServiceByName(ArcturusEntityService.SERVICE_NAME_USER)
				.send(useCaseId,
					(entityId != null) ? entityId : userId,
					requestId,
					userId,
					jsonPayload
				);
		}
		else
		{
			determineServiceAndSend(requestId, userId, useCaseId, service, entityId, jsonPayload);
		}
	}

	public EntityServiceProvider getEntityServiceProvider()
	{
		return entityServiceProvider;
	}

	public UserSessionHandler getUserSessionHandler()
	{
		return userSessionHandler;
	}

	public SwaggerInfo getSwaggerInfo()
	{
		return swaggerInfo;
	}

	public PrivateUseCases getPrivateUseCases()
	{
		return privateUseCases;
	}

	public ArcturusEntityService getAppLoggerService()
	{
		return appLoggerEntityService;
	}
}
