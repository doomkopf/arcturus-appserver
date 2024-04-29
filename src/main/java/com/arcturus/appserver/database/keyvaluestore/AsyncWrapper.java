package com.arcturus.appserver.database.keyvaluestore;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a {@link SyncKeyValueStore} making it asynchronous.
 *
 * @author doomkopf
 */
public class AsyncWrapper<K, V> implements KeyValueStore<K, V>
{
	private final Logger log;
	private final SyncKeyValueStore<K, V> syncStore;
	private final ExecutorService executorService;

	public AsyncWrapper(
		LoggerFactory loggerFactory,
		SyncKeyValueStore<K, V> syncStore,
		ExecutorService executorService
	)
	{
		this.log = loggerFactory.create(getClass());
		this.syncStore = syncStore;
		this.executorService = executorService;
	}

	@Override
	public void asyncGet(K key, GetResultHandler<K, V> resultHandler)
	{
		executorService.execute(() -> resultHandler.handle(key, syncStore.get(key)));
	}

	@Override
	public void asyncPut(K key, V value, PutResultHandler<K> resultHandler)
	{
		executorService.execute(() ->
		{
			var result = syncStore.put(key, value);
			if (resultHandler != null)
			{
				resultHandler.handle(key, result ? PutResult.ok : PutResult.unknownError);
			}
		});
	}

	@Override
	public void asyncRemove(K key, RemoveResultHandler<K> resultHandler)
	{
		executorService.execute(() ->
		{
			var result = syncStore.remove(key);
			if (resultHandler != null)
			{
				resultHandler.handle(key, result ? RemoveResult.ok : RemoveResult.unknownError);
			}
		});
	}

	@Override
	public void shutdown() throws InterruptedException
	{
		log.log(LogLevel.info, "Shutting down...");

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		syncStore.shutdown();

		log.log(LogLevel.info, "Done shutting down");
	}
}