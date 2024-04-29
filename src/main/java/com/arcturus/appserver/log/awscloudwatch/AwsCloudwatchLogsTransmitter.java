package com.arcturus.appserver.log.awscloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class AwsCloudwatchLogsTransmitter
{
	private static List<InputLogEvent> createEmptyLogEventsList()
	{
		return Collections.synchronizedList(new LinkedList<>());
	}

	private final AtomicReference<List<InputLogEvent>> logEventsRef = new AtomicReference<>(
		createEmptyLogEventsList());
	private final ScheduledExecutorService transferService;

	private final String logGroupName;
	private final String logStreamName = System.currentTimeMillis() + "-" + UUID.randomUUID();

	private final AWSLogsClient awsLogsClient;
	private String sequenceToken = null;

	AwsCloudwatchLogsTransmitter(Config config)
	{
		var creds = new BasicAWSCredentials(config.getString(ServerConfigPropery.awsCloudwatchLogsAccessKey),
			config.getString(ServerConfigPropery.awsCloudwatchLogsSecretKey)
		);

		logGroupName = config.getString(ServerConfigPropery.awsCloudwatchLogsLogGroupName);

		awsLogsClient = (AWSLogsClient) AWSLogsClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(creds))
			.withRegion(Regions.fromName(config.getString(ServerConfigPropery.awsCloudwatchLogsRegion)))
			.build();

		var createLogStream = new CreateLogStreamRequest(logGroupName, logStreamName);
		awsLogsClient.createLogStream(createLogStream);

		var delay = config.getInt(ServerConfigPropery.awsCloudwatchLogsTransferDelayMillis);
		transferService = Executors.newSingleThreadScheduledExecutor();
		transferService.scheduleWithFixedDelay(this::transferLogEvents,
			delay,
			delay,
			TimeUnit.MILLISECONDS
		);
	}

	void send(InputLogEvent logEvent)
	{
		logEventsRef.get().add(logEvent);
	}

	private void transferLogEvents()
	{
		var logEvents = logEventsRef.get();
		if (logEvents.isEmpty())
		{
			return;
		}

		logEvents = logEventsRef.getAndSet(createEmptyLogEventsList());

		var put = new PutLogEventsRequest(logGroupName, logStreamName, logEvents);

		if (sequenceToken != null)
		{
			put.setSequenceToken(sequenceToken);
		}
		var result = awsLogsClient.putLogEvents(put);
		sequenceToken = result.getNextSequenceToken();
	}

	void shutdown() throws InterruptedException
	{
		transferService.shutdown();
		try
		{
			transferService.awaitTermination(4000, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		finally
		{
			awsLogsClient.shutdown();
		}
	}
}