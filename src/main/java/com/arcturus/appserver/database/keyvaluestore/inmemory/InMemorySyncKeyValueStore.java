package com.arcturus.appserver.database.keyvaluestore.inmemory;

import com.arcturus.appserver.database.keyvaluestore.SyncKeyValueStore;

import java.util.concurrent.ConcurrentMap;

/**
 * An in-memory implementation of {@link SyncKeyValueStore}.
 *
 * @author doomkopf
 */
public class InMemorySyncKeyValueStore<K, V> implements SyncKeyValueStore<K, V>
{
	private final ConcurrentMap<K, V> map;

	public InMemorySyncKeyValueStore(ConcurrentMap<K, V> map)
	{
		this.map = map;
	}

	@Override
	public V get(K key)
	{
		return map.get(key);
	}

	@Override
	public boolean put(K key, V value)
	{
		map.put(key, value);
		return true;
	}

	@Override
	public boolean remove(K key)
	{
		map.remove(key);
		return true;
	}

	@Override
	public void shutdown()
	{
	}
}