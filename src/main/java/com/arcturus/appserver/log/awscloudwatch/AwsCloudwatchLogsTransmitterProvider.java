package com.arcturus.appserver.log.awscloudwatch;

import com.arcturus.appserver.config.Config;

public class AwsCloudwatchLogsTransmitterProvider
{
	private final Config config;

	private AwsCloudwatchLogsTransmitter transmitter = null;

	public AwsCloudwatchLogsTransmitterProvider(Config config)
	{
		this.config = config;
	}

	public synchronized AwsCloudwatchLogsTransmitter getOrCreate()
	{
		if (transmitter == null)
		{
			transmitter = new AwsCloudwatchLogsTransmitter(config);
		}

		return transmitter;
	}

	synchronized void shutdown() throws InterruptedException
	{
		if (transmitter != null)
		{
			transmitter.shutdown();
			transmitter = null;
		}
	}
}