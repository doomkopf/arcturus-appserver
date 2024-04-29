package com.arcturus.appserver.system.internalapp.maintainer.usecase.openapplog;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.concurrent.ArcturusExecutor;
import com.arcturus.appserver.system.AppManager;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.logmessage.LogMessageUseCase;
import com.arcturus.appserver.system.app.logmessage.OpenAppLog.Request;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.internalapp.maintainer.service.maintainer.Maintainer;

import java.util.UUID;

@EntityUseCase(id = InternalUseCases.OPEN_APP_LOG, service = ArcturusEntityService.SERVICE_NAME_USER, isPublic = true)
public class OpenAppLog extends PojoPayloadEntityUseCaseHandler<Maintainer, OpenAppLogRequest>
{
	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final AppManager appManager;
	private final ArcturusExecutor arcturusExecutor;

	public OpenAppLog(
		LoggerFactory loggerFactory,
		JsonStringSerializer jsonStringSerializer,
		AppManager appManager,
		ArcturusExecutor arcturusExecutor
	)
	{
		super(jsonStringSerializer);
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.appManager = appManager;
		this.arcturusExecutor = arcturusExecutor;
	}

	@Override
	protected Class<OpenAppLogRequest> getPayloadType()
	{
		return OpenAppLogRequest.class;
	}

	@Override
	protected void handle(
		Maintainer entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		OpenAppLogRequest payload,
		UseCaseContext context
	)
	{
		if (entity.getApps().stream().noneMatch(app -> app.getId().equals(payload.getAppId())))
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(
					LogLevel.debug,
					"Maintainer " + id + " tried to access not owned app " + payload.getAppId()
				);
			}
			return;
		}

		arcturusExecutor.execute(() ->
		{
			var app = appManager.getApp(payload.getAppId());
			if (app == null)
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, "App not found: " + payload.getAppId());
				}
				return;
			}

			app.getAppLoggerService().send(
				LogMessageUseCase.ol.name(),
				Tools.currentDayUUID(),
				0,
				requestingUserId,
				jsonStringSerializer.toJsonString(new Request())
			);

		});
	}
}