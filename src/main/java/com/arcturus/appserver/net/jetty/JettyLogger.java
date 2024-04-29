package com.arcturus.appserver.net.jetty;

import org.eclipse.jetty.util.log.AbstractLogger;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

/**
 * In order to make jetty writing log output to where you want it, you have to
 * extend {@link AbstractLogger}. This one delegates all calls to our
 * {@link ArcturusLogger}.
 * 
 * @author doomkopf
 */
public class JettyLogger extends AbstractLogger
{
	private final LoggerFactory loggerFactory;
	private final Logger log;

	public JettyLogger(LoggerFactory loggerFactory, String name)
	{
		this.loggerFactory = loggerFactory;
		this.log = loggerFactory.create(name);
	}

	@Override
	public String getName()
	{
		return log.getName();
	}

	@Override
	public void warn(String msg, Object... args)
	{
		log.log(LogLevel.warn, msg, args);
	}

	@Override
	public void warn(Throwable thrown)
	{
		log.log(LogLevel.warn, thrown);
	}

	@Override
	public void warn(String msg, Throwable thrown)
	{
		log.log(LogLevel.warn, msg, thrown);
	}

	@Override
	public void info(String msg, Object... args)
	{
		log.log(LogLevel.info, msg, args);
	}

	@Override
	public void info(Throwable thrown)
	{
		log.log(LogLevel.info, thrown);
	}

	@Override
	public void info(String msg, Throwable thrown)
	{
		log.log(LogLevel.info, msg, thrown);
	}

	@Override
	public boolean isDebugEnabled()
	{
		return log.isLogLevel(LogLevel.debug);
	}

	@Override
	public void setDebugEnabled(boolean enabled)
	{
		// Nothing
	}

	@Override
	public void debug(String msg, Object... args)
	{
		log.log(LogLevel.debug, msg, args);
	}

	@Override
	public void debug(Throwable thrown)
	{
		log.log(LogLevel.debug, thrown);
	}

	@Override
	public void debug(String msg, Throwable thrown)
	{
		log.log(LogLevel.debug, msg, thrown);
	}

	@Override
	public void ignore(Throwable ignored)
	{
		log.log(LogLevel.debug, ignored);
	}

	@Override
	protected org.eclipse.jetty.util.log.Logger newLogger(String fullname)
	{
		return new JettyLogger(loggerFactory, fullname);
	}
}