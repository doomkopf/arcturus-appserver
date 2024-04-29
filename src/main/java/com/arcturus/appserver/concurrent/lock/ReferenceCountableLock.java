package com.arcturus.appserver.concurrent.lock;

import java.util.concurrent.locks.ReentrantLock;

class ReferenceCountableLock extends ReentrantLock
{
	private static final long serialVersionUID = 1L;

	private int referenceCount;

	ReferenceCountableLock()
	{
		referenceCount = 0;
	}

	int getReferenceCount()
	{
		return referenceCount;
	}

	void incrementReferenceCount()
	{
		referenceCount++;
	}

	void decrementReferenceCount()
	{
		referenceCount--;
	}
}
