package com.arcturus.appserver.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ArcturusScheduledExecutor
{
	private final ScheduledExecutorService scheduledExecutorService;

	public ArcturusScheduledExecutor()
	{
		// TODO config
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
	{
		return scheduledExecutorService.schedule(command, delay, unit);
	}

	public ScheduledFuture<?> scheduledRepeated(
		Runnable command, long initialDelay, long delay, TimeUnit unit
	)
	{
		return scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	public void shutdown() throws InterruptedException
	{
		scheduledExecutorService.shutdown();
		scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
	}
}