package com.arcturus.appserver.database.keyvaluestore;

import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.KeyValueStoreDatabaseType;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.database.couchbase.CouchbaseConnection;
import com.arcturus.appserver.database.keyvaluestore.awsdynamodb.AwsDDBStringKeyValueStore;
import com.arcturus.appserver.database.keyvaluestore.couchbase.CouchbaseStringKeyValueStore;
import com.arcturus.appserver.database.keyvaluestore.file.FileSyncKeyValueStore;
import com.arcturus.appserver.database.keyvaluestore.inmemory.InMemorySyncKeyValueStore;
import com.arcturus.appserver.database.keyvaluestore.inmemory.StringConcurrentMapContainer;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Creates {@link StringKeyValueStore} based on {@link Config}.
 *
 * @author doomkopf
 */
public class KeyValueStoreDatabaseFactory
{
	private final Config config;
	private final LoggerFactory loggerFactory;
	private final CouchbaseConnection couchbaseConnection;
	private final StringConcurrentMapContainer stringConcurrentMapContainer;

	public KeyValueStoreDatabaseFactory(
		Config config,
		LoggerFactory loggerFactory,
		CouchbaseConnection couchbaseConnection,
		StringConcurrentMapContainer stringConcurrentMapContainer
	)
	{
		this.config = config;
		this.loggerFactory = loggerFactory;
		this.couchbaseConnection = couchbaseConnection;
		this.stringConcurrentMapContainer = stringConcurrentMapContainer;
	}

	public StringKeyValueStore create() throws IOException
	{
		var type = config.getEnum(KeyValueStoreDatabaseType.class,
			ServerConfigPropery.keyValueStoreDatabaseType
		);
		switch (type)
		{
		case file:
			return new StringAsyncWrapper(loggerFactory,
				new FileSyncKeyValueStore(config),
				Executors.newFixedThreadPool(config.getInt(ServerConfigPropery.fileDatabaseThreads))
			);
		case couchbase:
			return new CouchbaseStringKeyValueStore(loggerFactory, couchbaseConnection);
		case inMemory:
			return new StringAsyncWrapper(loggerFactory,
				new InMemorySyncKeyValueStore<>(stringConcurrentMapContainer.getConcurrentMap()),
				Executors.newSingleThreadExecutor()
			);
		case awsDynamoDb:
			return new AwsDDBStringKeyValueStore(loggerFactory, config);
		default:
			break;
		}

		return null;
	}
}