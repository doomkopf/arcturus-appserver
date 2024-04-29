package com.arcturus.appserver.log.sysout;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

/**
 * A sysout based implementation of {@link LoggerFactory}.
 * 
 * @author doomkopf
 */
public class SystemOutputLoggerFactory implements LoggerFactory
{
	private final LogLevel logLevel;

	public SystemOutputLoggerFactory(LogLevel logLevel)
	{
		this.logLevel = logLevel;
	}

	@Override
	public Logger create(Class<?> loggingClass)
	{
		return new SystemOutputLogger(logLevel, loggingClass.getCanonicalName());
	}

	@Override
	public Logger create(String name)
	{
		return new SystemOutputLogger(logLevel, name);
	}
}