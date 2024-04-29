package com.arcturus.appserver.log.awscloudwatch;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;

public class AwsCloudwatchLoggerFactory implements LoggerFactory
{
	private final AwsCloudwatchLogsTransmitter transmitter;
	private final LogLevel logLevel;
	private final LoggerFactory localLoggerFactory;

	public AwsCloudwatchLoggerFactory(
		Config config, AwsCloudwatchLogsTransmitter transmitter, LoggerFactory localLoggerFactory
	)
	{
		this.transmitter = transmitter;
		this.localLoggerFactory = localLoggerFactory;
		logLevel = config.getEnum(LogLevel.class, ServerConfigPropery.awsCloudwatchLogsLogLevel);
	}

	@Override
	public Logger create(Class<?> loggingClass)
	{
		return create(loggingClass.getCanonicalName());
	}

	@Override
	public Logger create(String name)
	{
		return new AwsCloudwatchLogger(
			transmitter,
			localLoggerFactory.create(name),
			logLevel,
			name
		);
	}
}