package com.arcturus.appserver.log.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.arcturus.api.LogLevel;

/**
 * A log4j2 based implementation of {@link ArcturusLogger}.
 * 
 * @author doomkopf
 */
public class Log4j2Logger implements com.arcturus.api.Logger
{
	private final Logger log;

	public Log4j2Logger(Class<?> loggingClass)
	{
		log = LogManager.getLogger(loggingClass);
	}

	public Log4j2Logger(String name)
	{
		log = LogManager.getLogger(name);
	}

	@Override
	public boolean isLogLevel(LogLevel logLevel)
	{
		switch (logLevel)
		{
		case error:
			return log.isErrorEnabled();
		case warn:
			return log.isWarnEnabled();
		case info:
			return log.isInfoEnabled();
		case debug:
			return log.isDebugEnabled();
		default:
			break;
		}

		return false;
	}

	@Override
	public void log(LogLevel logLevel, String message, Throwable throwable)
	{
		switch (logLevel)
		{
		case error:
			log.error(message, throwable);
			break;
		case warn:
			log.warn(message, throwable);
			break;
		case info:
			log.info(message, throwable);
			break;
		case debug:
			log.debug(message, throwable);
			break;
		default:
			break;
		}
	}

	@Override
	public void log(LogLevel logLevel, String message)
	{
		switch (logLevel)
		{
		case error:
			log.error(message);
			break;
		case warn:
			log.warn(message);
			break;
		case info:
			log.info(message);
			break;
		case debug:
			log.debug(message);
			break;
		default:
			break;
		}
	}

	@Override
	public void log(LogLevel logLevel, String message, Object... args)
	{
		switch (logLevel)
		{
		case error:
			log.error(message, args);
			break;
		case warn:
			log.warn(message, args);
			break;
		case info:
			log.info(message, args);
			break;
		case debug:
			log.debug(message, args);
			break;
		default:
			break;
		}
	}

	@Override
	public void log(LogLevel logLevel, Throwable throwable)
	{
		switch (logLevel)
		{
		case error:
			log.error("", throwable);
			break;
		case warn:
			log.warn("", throwable);
			break;
		case info:
			log.info("", throwable);
			break;
		case debug:
			log.debug("", throwable);
			break;
		default:
			break;
		}
	}

	@Override
	public String getName()
	{
		return log.getName();
	}
}