package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.LoggerFactory;
import com.arcturus.api.log.AppLogLevel;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.app.inject.AppId;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;
import com.arcturus.appserver.system.app.logmessage.LogMessage;

import java.util.UUID;

public class JsAppLogger extends ArcturusAppLogger
{
	private static class NewAppLogMessage
	{
		final String uc = InternalUseCases.NEW_APP_LOG_MESSAGE;
		final LogMessage m;

		NewAppLogMessage(LogMessage m)
		{
			this.m = m;
		}
	}

	private final UUID maintainerUserId;
	private final ArcturusUserSender userSender;
	private final JsonStringSerializer jsonStringSerializer;

	public JsAppLogger(
		LoggerFactory loggerFactory,
		@AppId
			String appId,
		UUID maintainerUserId,
		ArcturusUserSender userSender,
		JsonStringSerializer jsonStringSerializer
	)
	{
		super(loggerFactory, appId);
		this.maintainerUserId = maintainerUserId;
		this.userSender = userSender;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public void log(AppLogLevel level, String message)
	{
		super.log(level, message);

		// BAAS logging
		/*userSender.send(maintainerUserId,
			jsonStringSerializer.toJsonString(new NewAppLogMessage(new LogMessage(System.currentTimeMillis(),
				level,
				message
			)))
		);*/
	}
}