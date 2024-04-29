package com.arcturus.appserver.concurrent.context;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class ContextExecutableExecutorService
{
	private final Logger log;
	private final Queue<Runnable> runnables;
	private final InternalThread[] internalThreads;

	public ContextExecutableExecutorService(LoggerFactory loggerFactory, int threads)
	{
		log = loggerFactory.create(getClass());

		runnables = new LinkedList<>();

		internalThreads = new InternalThread[threads];
		for (var i = 0; i < threads; i++)
		{
			var thread = new InternalThread(loggerFactory, runnables, "CE-InternalThread" + i);
			thread.start();
			internalThreads[i] = thread;
		}
	}

	public void execute(Runnable r)
	{
		synchronized (runnables)
		{
			if (!runnables.offer(r))
			{
				log.log(LogLevel.error, "Queue full - dropping message");
			}
			else
			{
				runnables.notify();
			}
		}
	}

	public void shutdown() throws InterruptedException
	{
		for (var thread : internalThreads)
		{
			try
			{
				thread.shutdown(1000);
			}
			catch (InterruptedException e)
			{
				throw e;
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}
	}
}