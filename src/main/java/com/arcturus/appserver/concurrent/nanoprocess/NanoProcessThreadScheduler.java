package com.arcturus.appserver.concurrent.nanoprocess;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Schedules the processing of one {@link NanoProcessThread}.
 *
 * @author doomkopf
 */
public class NanoProcessThreadScheduler
{
	private final long waitTimeMillis;
	private final AtomicLong queuedMessageCount = new AtomicLong(0);
	private final AtomicLong queuedContextExecutionsCount = new AtomicLong(0);
	private final long deltaTime;
	private long nextTimeOver;

	NanoProcessThreadScheduler(long waitTimeMillis)
	{
		this.waitTimeMillis = waitTimeMillis;
		deltaTime = waitTimeMillis;

		synchronized (this)
		{
			nextTimeOver = System.currentTimeMillis();
			scheduleNext();
		}
	}

	private void scheduleNext()
	{
		nextTimeOver += waitTimeMillis;
	}

	private boolean isTimeOver()
	{
		return System.currentTimeMillis() >= nextTimeOver;
	}

	private long calcWaitTime()
	{
		return Math.max(nextTimeOver - System.currentTimeMillis(), 0);
	}

	public long getDeltaTime()
	{
		return deltaTime;
	}

	void messageAdded()
	{
		queuedMessageCount.incrementAndGet();
		synchronized (this)
		{
			notify();
		}
	}

	void messageRemoved()
	{
		queuedMessageCount.decrementAndGet();
	}

	void contextExecutionAdded()
	{
		queuedContextExecutionsCount.incrementAndGet();
		synchronized (this)
		{
			notify();
		}
	}

	void contextExecutionRemoved()
	{
		queuedContextExecutionsCount.decrementAndGet();
	}

	/**
	 * @return True if a message was received or false if it returned because
	 * the time is over
	 * @throws InterruptedException
	 */
	boolean waitForMessages() throws InterruptedException
	{
		if ((queuedMessageCount.get() == 0) && (queuedContextExecutionsCount.get() == 0))
		{
			synchronized (this)
			{
				if ((queuedMessageCount.get() == 0) && (queuedContextExecutionsCount.get() == 0))
				{
					wait(); // Just wait for now, enable below code again when you need the update functions
					/*var waitTime = calcWaitTime();
					if (waitTime != 0)
					{
						wait(waitTime);
					}
					if (isTimeOver())
					{
						scheduleNext();
						return false;
					}*/
				}
			}
		}

		return true;
	}
}