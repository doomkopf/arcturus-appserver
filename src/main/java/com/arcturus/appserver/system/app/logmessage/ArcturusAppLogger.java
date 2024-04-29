package com.arcturus.appserver.system.app.logmessage;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.log.AppLogLevel;
import com.arcturus.api.log.AppLogger;
import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.appserver.system.app.inject.AppId;

public class ArcturusAppLogger implements AppLogger
{
	private static final int MAX_MESSAGE_LENGTH = 128;

	private final Logger log;
	private final String appId;
	private final AppLogLevel logLevel = AppLogLevel.DEBUG; // TODO make configurable for user
	private ListService<LogMessage> listChunkService;

	public ArcturusAppLogger(
		LoggerFactory loggerFactory,
		@AppId
			String appId
	)
	{
		log = loggerFactory.create(getClass());
		this.appId = appId;
	}

	public void init(AppLoggerEntityService appLoggerEntityService)
	{
		listChunkService = appLoggerEntityService.getListService();
	}

	@Override
	public void log(AppLogLevel level, String message)
	{
		// BAAS logging
		/*if (logLevel.isLogLevel(level))
		{
			if ((message == null) || message.isBlank() || (message.length() > MAX_MESSAGE_LENGTH))
			{
				return;
			}

			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "AppLog " + appId + ": " + message);
			}

			listChunkService.add(new LogMessage(System.currentTimeMillis(), level, message),
				Tools.currentDayUUID()
			);
		}*/

		LogLevel logLevel;
		switch (level)
		{
		case ERROR:
			logLevel = LogLevel.error;
			break;
		case WARN:
			logLevel = LogLevel.warn;
			break;
		case INFO:
			logLevel = LogLevel.info;
			break;
		case DEBUG:
		default:
			logLevel = LogLevel.debug;
			break;
		}

		if (log.isLogLevel(logLevel))
		{
			log.log(logLevel, "AppLog " + appId + ": " + message);
		}
	}
}