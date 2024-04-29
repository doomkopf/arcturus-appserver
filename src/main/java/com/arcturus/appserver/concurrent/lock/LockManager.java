package com.arcturus.appserver.concurrent.lock;

import java.util.HashMap;
import java.util.Map;

public class LockManager<K>
{
	private final Map<K, ReferenceCountableLock> locks = new HashMap<>();

	public void lock(K key)
	{
		ReferenceCountableLock lock;
		synchronized (locks)
		{
			lock = locks.get(key);
			if (lock == null)
			{
				lock = new ReferenceCountableLock();
				locks.put(key, lock);
			}
			lock.incrementReferenceCount();
		}
		lock.lock();
	}

	public void unlock(K key)
	{
		ReferenceCountableLock lock;
		synchronized (locks)
		{
			lock = locks.get(key);
			if ((lock == null) || !lock.isHeldByCurrentThread())
			{
				return;
			}
			lock.decrementReferenceCount();
			if (lock.getReferenceCount() <= 0)
			{
				locks.remove(key);
			}
		}
		lock.unlock();
	}
}
