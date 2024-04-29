package com.arcturus.appserver.log;

import com.arcturus.api.LogLevel;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.LoggerType;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.log.awscloudwatch.AwsCloudwatchLoggerFactory;
import com.arcturus.appserver.log.awscloudwatch.AwsCloudwatchLogsTransmitterProvider;
import com.arcturus.appserver.log.log4j2.Log4j2LoggerFactory;
import com.arcturus.appserver.log.sysout.SystemOutputLoggerFactory;

/**
 * A factory that creates {@link LoggerFactory} based on {@link Config}.
 *
 * @author doomkopf
 */
public class LoggerFactoryFactory
{
	private final Config config;
	private final AwsCloudwatchLogsTransmitterProvider awsCloudwatchLogsTransmitterProvider;

	public LoggerFactoryFactory(
		Config config, AwsCloudwatchLogsTransmitterProvider awsCloudwatchLogsTransmitterProvider
	)
	{
		this.config = config;
		this.awsCloudwatchLogsTransmitterProvider = awsCloudwatchLogsTransmitterProvider;
	}

	public LoggerFactory create()
	{
		switch (config.getEnum(LoggerType.class, ServerConfigPropery.logger))
		{
		case log4j2:
			return new Log4j2LoggerFactory();
		case sysout:
			return new SystemOutputLoggerFactory(LogLevel.debug);
		case awsCloudwatch:
			return new AwsCloudwatchLoggerFactory(
				config,
				awsCloudwatchLogsTransmitterProvider.getOrCreate(),
				new Log4j2LoggerFactory()
			);
		default:
			break;
		}

		return null;
	}
}