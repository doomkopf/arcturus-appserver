package com.arcturus.appserver.system;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates 64bit ids based on a unique (hopefully cause random) 32bit nodeId
 * and a 32bit incrementing number.
 * 
 * @author doomkopf
 */
public class IdGenerator
{
	private final int nodeId;
	private final AtomicInteger counter = new AtomicInteger();

	public IdGenerator()
	{
		nodeId = ThreadLocalRandom.current().nextInt();
	}

	public long generate()
	{
		return Tools.integersToLong(nodeId, counter.getAndIncrement());
	}
}