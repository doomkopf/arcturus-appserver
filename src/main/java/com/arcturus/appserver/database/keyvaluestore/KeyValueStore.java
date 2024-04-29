package com.arcturus.appserver.database.keyvaluestore;

/**
 * A store (repository or database) to asynchronously get, put and remove values
 * based on keys almost like in a map.
 *
 * @author doomkopf
 */
public interface KeyValueStore<K, V>
{
	enum PutResult
	{
		ok,
		casMismatch,
		unknownError
	}

	enum RemoveResult
	{
		ok,
		unknownError
	}

	@FunctionalInterface
	interface GetResultHandler<K, V>
	{
		void handle(K key, V value);
	}

	@FunctionalInterface
	interface PutResultHandler<K>
	{
		void handle(K key, PutResult putResult);
	}

	@FunctionalInterface
	interface RemoveResultHandler<K>
	{
		void handle(K key, RemoveResult removeResult);
	}

	PutResultHandler NOOP_PUT_RESULT_HANDLER = (key, result) ->
	{
	};

	RemoveResultHandler NOOP_REMOVE_RESULT_HANDLER = (key, result) ->
	{
	};

	void asyncGet(K key, GetResultHandler<K, V> resultHandler);

	void asyncPut(K key, V value, PutResultHandler<K> resultHandler);

	void asyncRemove(K key, RemoveResultHandler<K> resultHandler);

	void shutdown() throws InterruptedException;
}
