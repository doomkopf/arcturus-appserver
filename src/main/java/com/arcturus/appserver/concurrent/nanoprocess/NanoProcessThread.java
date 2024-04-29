package com.arcturus.appserver.concurrent.nanoprocess;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.concurrent.context.ContextExecutableThread;
import com.arcturus.appserver.concurrent.context.JavaThreadWithParent;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One physical thread holding multiple {@link NanoProcess}s and processing all
 * its messages.
 *
 * @author doomkopf
 */
public class NanoProcessThread<T> implements Runnable, ContextExecutableThread
{
	private static class RunnableWithCallback
	{
		final Runnable runnable;
		final Callback callback;

		RunnableWithCallback(
			Runnable runnable, Callback callback
		)
		{
			this.runnable = runnable;
			this.callback = callback;
		}
	}

	private static final AtomicInteger threadIdCounter = new AtomicInteger(0);

	private final Logger log;
	private final JavaThreadWithParent thread = new JavaThreadWithParent(
		this,
		this,
		NanoProcessThread.class.getSimpleName() + '-' + threadIdCounter.getAndIncrement()
	);
	final Set<NanoProcess<T>> nanoProcs = Collections.newSetFromMap(new ConcurrentHashMap<>());
	final NanoProcessThreadScheduler scheduler;

	private final Queue<RunnableWithCallback> contextExecutions = new ConcurrentLinkedQueue<>();

	NanoProcessThread(LoggerFactory loggerFactory, long scheduledIntervalMillis)
	{
		log = loggerFactory.create(getClass());

		scheduler = new NanoProcessThreadScheduler(scheduledIntervalMillis);
	}

	public void start()
	{
		thread.start();
	}

	void addProc(NanoProcess<T> proc)
	{
		nanoProcs.add(proc);
	}

	void removeProc(NanoProcess<T> proc)
	{
		nanoProcs.remove(proc);
	}

	public void shutdown() throws InterruptedException
	{
		thread.interrupt();
		thread.join(1000);
	}

	public int getLoad()
	{
		return nanoProcs.size();
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				if (!scheduler.waitForMessages())
				{
					handleScheduled();
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				break;
			}

			handleContextExecutions();
			handleMessages();
		}
	}

	private void handleMessages()
	{
		for (var proc : nanoProcs)
		{
			// Right, for full consistency we should synchronize the whole loop
			// on "proc" in order to write each state transition back to main
			// memory.
			// But it's costly and another thread will only be reading it on
			// shutdown. The current decision is uptime performance > shutdown
			// consistency.
			T msg;
			while ((msg = proc.messageQueue.poll()) != null)
			{
				scheduler.messageRemoved();
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

	private void handleContextExecutions()
	{
		if (contextExecutions.isEmpty())
		{
			return;
		}

		RunnableWithCallback contextExecution;
		while ((contextExecution = contextExecutions.poll()) != null)
		{
			scheduler.contextExecutionRemoved();
			try
			{
				contextExecution.runnable.run();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}

			try
			{
				contextExecution.callback.callback();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}
	}

	private void handleScheduled()
	{
		for (var proc : nanoProcs)
		{
			try
			{
				proc.handleScheduled(System.currentTimeMillis(), scheduler.getDeltaTime());
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

	@Override
	public void execute(
		Runnable runnable, Callback callback
	)
	{
		contextExecutions.offer(new RunnableWithCallback(runnable, callback));
		scheduler.contextExecutionAdded();
	}
}