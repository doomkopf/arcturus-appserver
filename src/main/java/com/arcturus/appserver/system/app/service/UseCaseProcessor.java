package com.arcturus.appserver.system.app.service;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.log.AppLogLevel;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCaseHandler;
import com.arcturus.appserver.concurrent.ArcturusContextExecutor;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;

import java.util.UUID;

public class UseCaseProcessor
{
	private final Logger log;
	private final UseCaseProvider useCaseProvider;
	private final ArcturusResponseSender responseSender;
	private final ArcturusUserSender userSender;
	private final ArcturusAppLogger appLogger;
	private final ArcturusContextExecutor contextExecutor;

	public UseCaseProcessor(
		LoggerFactory loggerFactory,
		UseCaseProvider useCaseProvider,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		ArcturusAppLogger appLogger,
		ArcturusContextExecutor contextExecutor
	)
	{
		log = loggerFactory.create(getClass());
		this.useCaseProvider = useCaseProvider;
		this.responseSender = responseSender;
		this.userSender = userSender;
		this.appLogger = appLogger;
		this.contextExecutor = contextExecutor;
	}

	public boolean process(
		String useCaseId,
		long requestId,
		UUID requestingUserId,
		String payload,
		RequestInfo requestInfo
	)
	{
		var useCaseHandler = useCaseProvider.getUseCaseHandler(useCaseId);
		if (useCaseHandler == null)
		{
			return false;
		}

		contextExecutor.execute(() -> handle(useCaseHandler,
			useCaseId,
			requestId,
			requestingUserId,
			payload,
			requestInfo
		));

		return true;
	}

	private void handle(
		UseCaseHandler useCaseHandler,
		String useCaseId,
		long requestId,
		UUID requestingUserId,
		String payload,
		RequestInfo requestInfo
	)
	{
		try
		{
			useCaseHandler.handle(requestId, requestingUserId, payload, requestInfo);
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.error))
			{
				log.log(LogLevel.error, e);
			}

			appLogger.log(AppLogLevel.ERROR, e.getMessage());

			NetCodes.sendErrorToPotentialClient(responseSender,
				userSender,
				requestId,
				requestingUserId,
				"",
				useCaseId,
				e
			);
		}
	}
}