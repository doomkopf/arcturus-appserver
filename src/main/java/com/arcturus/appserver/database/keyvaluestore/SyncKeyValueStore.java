package com.arcturus.appserver.database.keyvaluestore;

/**
 * The synchronous version of a key-value store
 *
 * @author doomkopf
 */
public interface SyncKeyValueStore<K, V>
{
	V get(K key);

	boolean put(K key, V value);

	boolean remove(K key);

	void shutdown() throws InterruptedException;
}