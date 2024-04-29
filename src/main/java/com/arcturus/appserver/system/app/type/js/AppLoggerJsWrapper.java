package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.log.AppLogLevel;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;

public class AppLoggerJsWrapper
{
	private final ArcturusAppLogger appLogger;

	public AppLoggerJsWrapper(ArcturusAppLogger appLogger)
	{
		this.appLogger = appLogger;
	}

	public void log(int level, String message)
	{
		appLogger.log(AppLogLevel.values()[level], message);
	}
}