package com.arcturus.appserver.net;

import java.util.concurrent.atomic.LongAdder;

public class PersistentLocalSessionStats
{
	private volatile long ddosLastMessage = 0;
	public final LongAdder ddosHighFrequencyMessageCount = new LongAdder();

	public long getDdosLastMessage()
	{
		return ddosLastMessage;
	}

	public void setDdosLastMessage(long ddosLastMessage)
	{
		this.ddosLastMessage = ddosLastMessage;
	}
}