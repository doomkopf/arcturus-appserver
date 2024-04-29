package com.arcturus.appserver.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ArcturusExecutor
{
	private final ExecutorService executorService;

	public ArcturusExecutor()
	{
		// TODO config
		executorService = new ThreadPoolExecutor(2,
			4,
			60,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(10000)
		);
	}

	public void execute(Runnable runnable)
	{
		executorService.execute(runnable);
	}

	public void shutdown() throws InterruptedException
	{
		executorService.shutdown();
		executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
	}
}