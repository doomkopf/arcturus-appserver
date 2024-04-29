package com.arcturus.appserver.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * For continuous calculation of an integer average value. This class is
 * threadsafe.
 * 
 * @author doomkopf
 */
public class Average
{
	private final LongAdder sum = new LongAdder();
	private final AtomicInteger count = new AtomicInteger(0);

	public void add(long value)
	{
		sum.add(value);
		count.incrementAndGet();
	}

	public long average()
	{
		return sum.sum() / count.get();
	}
}