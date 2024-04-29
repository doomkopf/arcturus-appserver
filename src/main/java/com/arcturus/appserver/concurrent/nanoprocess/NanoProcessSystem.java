package com.arcturus.appserver.concurrent.nanoprocess;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Root instance for the {@link NanoProcess} system. There should only be one
 * instance of it per VM.
 *
 * @author doomkopf
 */
public class NanoProcessSystem<T>
{
	private final Logger log;
	private final NanoProcessThread<T>[] threads;

	@SuppressWarnings("unchecked")
	public NanoProcessSystem(LoggerFactory loggerFactory, Config config)
	{
		log = loggerFactory.create(getClass());

		threads = new NanoProcessThread[Runtime.getRuntime().availableProcessors() * 2];
		for (var i = 0; i < threads.length; i++)
		{
			threads[i] = new NanoProcessThread<>(
				loggerFactory,
				config.getInt(ServerConfigPropery.nanoProcessScheduledIntervalMillis)
			);
		}
	}

	public void start()
	{
		for (var thread : threads)
		{
			thread.start();
		}
	}

	/**
	 * Creates and assigns a {@link NanoProcess} to a {@link NanoProcessThread}
	 * based on simple "least-load" load balancing.
	 *
	 * @param factory
	 * @return The new {@link NanoProcess}.
	 */
	public NanoProcess<T> addProc(NanoProcessFactory<T> factory)
	{
		// There are never 0 threads so call to just get without checking is ok
		var thread = Arrays.stream(threads)
			.min(Comparator.comparingInt(NanoProcessThread::getLoad))
			.get();

		var nanoProcess = factory.create(thread);
		thread.addProc(nanoProcess);

		return nanoProcess;
	}

	public void shutdown()
	{
		for (var thread : threads)
		{
			try
			{
				thread.shutdown();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}

		handleRemainingMessages();
	}

	private void handleRemainingMessages()
	{
		// To make sure the execution of a message didn't trigger another
		// message to be processed we need at least one loop cycle without any
		// messages
		var messageWasExecuted = false;
		do
		{
			for (var thread : threads)
			{
				for (var proc : thread.nanoProcs)
				{
					T msg;
					while ((msg = proc.messageQueue.poll()) != null)
					{
						messageWasExecuted = true;
						try
						{
							proc.handleMessage(msg);
						}
						catch (Throwable e)
						{
							if (log.isLogLevel(LogLevel.debug))
							{
								log.log(LogLevel.debug, e);
							}
						}
					}
				}
			}
		}
		while (messageWasExecuted);
	}
}