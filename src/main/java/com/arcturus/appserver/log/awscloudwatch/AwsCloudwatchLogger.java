package com.arcturus.appserver.log.awscloudwatch;

import com.amazonaws.services.logs.model.InputLogEvent;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class AwsCloudwatchLogger implements Logger
{
	private static String buildString(Throwable throwable)
	{
		try (var errors = new StringWriter())
		{
			throwable.printStackTrace(new PrintWriter(errors));
			return errors.toString();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	private final AwsCloudwatchLogsTransmitter transmitter;
	private final Logger localLogger;

	private final LogLevel logLevel;
	private final String name;

	AwsCloudwatchLogger(
		AwsCloudwatchLogsTransmitter transmitter, Logger localLogger, LogLevel logLevel, String name
	)
	{
		this.transmitter = transmitter;
		this.localLogger = localLogger;
		this.logLevel = logLevel;
		this.name = name;
	}

	@Override
	public boolean isLogLevel(LogLevel logLevel)
	{
		return this.logLevel.ordinal() >= logLevel.ordinal();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void log(LogLevel logLevel, String s)
	{
		if (!isLogLevel(logLevel))
		{
			return;
		}

		localLogger.log(logLevel, s);

		transmitter.send(new InputLogEvent().withTimestamp(System.currentTimeMillis())
			.withMessage(logLevel.name().toUpperCase(Locale.ENGLISH) + '-' + name + ": " + s));
	}

	@Override
	public void log(LogLevel logLevel, String s, Object... objects)
	{
		log(logLevel, String.format(s, objects));
	}

	@Override
	public void log(LogLevel logLevel, Throwable throwable)
	{
		log(logLevel, buildString(throwable));
	}

	@Override
	public void log(LogLevel logLevel, String s, Throwable throwable)
	{
		log(logLevel, s + '\n' + buildString(throwable));
	}
}