package com.arcturus.appserver.log.log4j2;

import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

/**
 * A log4j2 based implementation of {@link LoggerFactory}.
 * 
 * @author doomkopf
 */
public class Log4j2LoggerFactory implements LoggerFactory
{
	@Override
	public Logger create(Class<?> loggingClass)
	{
		return new Log4j2Logger(loggingClass);
	}

	@Override
	public Logger create(String name)
	{
		return new Log4j2Logger(name);
	}
}