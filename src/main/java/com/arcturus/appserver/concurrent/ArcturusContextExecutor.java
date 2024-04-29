package com.arcturus.appserver.concurrent;

import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.concurrent.context.ContextExecutableExecutorService;

public class ArcturusContextExecutor
{
	private final ContextExecutableExecutorService contextExecutorService;

	public ArcturusContextExecutor(LoggerFactory loggerFactory)
	{
		// TODO config
		contextExecutorService = new ContextExecutableExecutorService(loggerFactory, 8);
	}

	public void execute(Runnable r)
	{
		contextExecutorService.execute(r);
	}

	public void shutdown() throws InterruptedException
	{
		contextExecutorService.shutdown();
	}
}