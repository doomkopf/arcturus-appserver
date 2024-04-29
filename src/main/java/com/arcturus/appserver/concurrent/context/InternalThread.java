package com.arcturus.appserver.concurrent.context;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

import java.util.Queue;

public class InternalThread implements Runnable, ContextExecutableThread
{
	private final Logger log;
	private final JavaThreadWithParent thread;
	private final Queue<Runnable> sharedQueue;

	private Runnable runnable = null;
	private Callback callback = null;

	InternalThread(LoggerFactory loggerFactory, Queue<Runnable> sharedQueue, String name)
	{
		log = loggerFactory.create(getClass());
		this.sharedQueue = sharedQueue;
		thread = new JavaThreadWithParent(this, this, name);
	}

	void start()
	{
		thread.start();
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				Runnable r = null;
				Callback c = null;
				synchronized (sharedQueue)
				{
					if (sharedQueue.isEmpty() && (runnable == null))
					{
						sharedQueue.wait();
					}

					if (runnable != null)
					{
						saveRunRunnable(runnable);
						c = callback;

						runnable = null;
						callback = null;
					}
					else
					{
						r = sharedQueue.poll();
					}
				}

				if (c != null)
				{
					saveRunCallback(c);
				}

				if (r != null)
				{
					saveRunRunnable(r);
				}
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}

	@Override
	public void execute(Runnable runnable, Callback callback)
	{
		synchronized (sharedQueue)
		{
			if (this.runnable != null)
			{
				log.log(
					LogLevel.error,
					"About to assign a context execution while one is already assigned"
				);
			}
			this.runnable = runnable;
			this.callback = callback;
			sharedQueue.notifyAll();
		}
	}

	public void shutdown(long timeoutMillis) throws InterruptedException
	{
		thread.interrupt();
		thread.join(timeoutMillis);
	}

	private void saveRunRunnable(Runnable r)
	{
		try
		{
			r.run();
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}

	private void saveRunCallback(Callback c)
	{
		try
		{
			c.callback();
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}
}