package com.arcturus.appserver.concurrent.context;

/**
 * A special thread that allows the execution of {@link Runnable}s in its own context.
 * When done the callback is called.
 *
 * @author doomkopf
 */
public interface ContextExecutableThread
{
	interface Callback
	{
		void callback();
	}

	void execute(Runnable runnable, Callback callback);

	default void executeSync(Runnable runnable, long timeoutMillis) throws InterruptedException
	{
		var monitor = this;
		synchronized (monitor)
		{
			execute(runnable, () ->
			{
				synchronized (monitor)
				{
					monitor.notify();
				}
			});
			monitor.wait(timeoutMillis);
		}
	}
}