package com.arcturus.appserver.system;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppMaintenanceContext
{
	private volatile CountDownLatch countDownLatch = null;

	public void initExpectedNodesToCallBack(int count)
	{
		countDownLatch = new CountDownLatch(count);
	}

	public boolean waitForCallbacks() throws InterruptedException
	{
		return countDownLatch.await(
				Constants.APP_SHUTDOWN_WAIT_FOR_NODE_CALLBACKS_TIMEOUT_MILLIS,
				TimeUnit.MILLISECONDS);
	}

	public void callback()
	{
		countDownLatch.countDown();
	}
}