package com.arcturus.appserver.database.keyvaluestore.couchbase;

import java.util.concurrent.TimeUnit;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.database.couchbase.CouchbaseConnection;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;

import rx.Observer;

/**
 * A couchbase based implementation of {@link StringKeyValueStore}.
 * 
 * @author doomkopf
 */
public class CouchbaseStringKeyValueStore implements StringKeyValueStore
{
	private static final int READ_TIMEOUT_SECONDS = 4;
	private static final int WRITE_TIMEOUT_SECONDS = 4;

	final Logger log;
	private final AsyncBucket bucket;

	public CouchbaseStringKeyValueStore(
			LoggerFactory loggerFactory,
			CouchbaseConnection couchbaseConnection)
	{
		this.log = loggerFactory.create(getClass());
		this.bucket = couchbaseConnection.getAsyncBucket();
	}

	@Override
	public void asyncGet(String key, GetResultHandler<String, String> resultHandler)
	{
		bucket
				.get(key, RawJsonDocument.class)
				.singleOrDefault(null)
				.timeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.subscribe(new Observer<RawJsonDocument>()
				{

					@Override
					public void onCompleted()
					{
						// Nothing
					}

					@Override
					public void onError(Throwable e)
					{
						log.log(LogLevel.error, e);
						resultHandler.handle(key, null);
					}

					@Override
					public void onNext(RawJsonDocument t)
					{
						resultHandler.handle(key, t == null ? null : t.content());
					}
				});
	}

	@Override
	public void asyncPut(String key, String value, PutResultHandler<String> resultHandler)
	{
		bucket
				.upsert(RawJsonDocument.create(key, value))
				.timeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.subscribe(new Observer<RawJsonDocument>()
				{

					@Override
					public void onCompleted()
					{
						// Nothing
					}

					@Override
					public void onError(Throwable e)
					{
						log.log(LogLevel.error, e);
						if (resultHandler != null)
						{
							resultHandler.handle(key, PutResult.unknownError);
						}
					}

					@Override
					public void onNext(RawJsonDocument t)
					{
						if (resultHandler != null)
						{
							resultHandler.handle(key, PutResult.ok);
						}
					}
				});
	}

	@Override
	public void asyncRemove(String key, RemoveResultHandler<String> resultHandler)
	{
		bucket.remove(key).timeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS).subscribe(
				new Observer<JsonDocument>()
				{

					@Override
					public void onCompleted()
					{
						// Nothing
					}

					@Override
					public void onError(Throwable e)
					{
						log.log(LogLevel.error, e);
						if (resultHandler != null)
						{
							resultHandler.handle(key, RemoveResult.unknownError);
						}
					}

					@Override
					public void onNext(JsonDocument t)
					{
						if (resultHandler != null)
						{
							resultHandler.handle(key, RemoveResult.ok);
						}
					}
				});
	}

	@Override
	public void shutdown() throws InterruptedException
	{
		// Nothing
	}
}