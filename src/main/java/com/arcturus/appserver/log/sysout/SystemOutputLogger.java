package com.arcturus.appserver.log.sysout;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;

/**
 * A sysout based implementation of {@link Logger}.
 * 
 * @author doomkopf
 */
public class SystemOutputLogger implements Logger
{
	private final LogLevel logLevel;
	private final String name;

	public SystemOutputLogger(LogLevel logLevel, String name)
	{
		this.logLevel = logLevel;
		this.name = name;
	}

	@Override
	public boolean isLogLevel(LogLevel logLevel)
	{
		return this.logLevel.ordinal() >= logLevel.ordinal();
	}

	@Override
	public void log(LogLevel logLevel, String message)
	{
		if (!isLogLevel(logLevel))
		{
			return;
		}

		if (logLevel == LogLevel.error)
		{
			System.err.println(message); // NOSONAR I'm deliberately choosing
											// sysout here
		}
		else
		{
			System.out.println(message); // NOSONAR
		}
	}

	@Override
	public void log(LogLevel logLevel, String message, Object... args)
	{
		if (!isLogLevel(logLevel))
		{
			return;
		}

		if (logLevel == LogLevel.error)
		{
			System.err.format(message, args); // NOSONAR
		}
		else
		{
			System.out.format(message); // NOSONAR
		}
	}

	@Override
	public void log(LogLevel logLevel, Throwable throwable)
	{
		if (!isLogLevel(logLevel))
		{
			return;
		}

		if (logLevel == LogLevel.error)
		{
			throwable.printStackTrace(System.err); // NOSONAR
		}
		else
		{
			throwable.printStackTrace(System.out); // NOSONAR
		}
	}

	@Override
	public void log(LogLevel logLevel, String message, Throwable throwable)
	{
		log(logLevel, message);
		log(logLevel, throwable);
	}

	@Override
	public String getName()
	{
		return name;
	}
}