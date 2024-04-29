package com.arcturus.appserver.concurrent.nanoprocess;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.ServerConfigPropery;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A lightweight process that holds an isolated state.
 *
 * @author doomkopf
 */
public abstract class NanoProcess<T>
{
	private final Logger log;
	final Queue<T> messageQueue = new ConcurrentLinkedQueue<>();
	private final NanoProcessThread<T> thread;

	protected NanoProcess(NanoProcessThread<T> thread, LoggerFactory loggerFactory)
	{
		log = loggerFactory.create(getClass());
		this.thread = thread;
	}

	public void queueMessage(T msg)
	{
		messageQueue.offer(msg);
		thread.scheduler.messageAdded();
	}

	/**
	 * Must be called from its own context - never from the outside.
	 */
	protected void kill(boolean handleRemainingMessages)
	{
		if (handleRemainingMessages)
		{
			for (var msg : messageQueue)
			{
				try
				{
					handleMessage(msg);
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

		thread.removeProc(this);
	}

	/**
	 * The handling of a message. Isolated state is safe to be touched here. No
	 * I/O access or other blocking calls should be done here.
	 *
	 * @param msg the message to be processed
	 */
	protected abstract void handleMessage(T msg);

	/**
	 * Called repeatedly after a delay configured in
	 * {@link ServerConfigPropery#nanoProcessScheduledIntervalMillis}. No I/O
	 * access or other blocking calls should be done here.
	 *
	 * @param deltaTime
	 */
	protected abstract void handleScheduled(long now, long deltaTime);
}