package com.arcturus.appserver.database.couchbase;

import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;

/**
 * Wraps the global couchbase connection types and configures them.
 * 
 * @author doomkopf
 */
public class CouchbaseConnection
{
	public final CouchbaseCluster cluster;
	public final Bucket bucket;
	public final AsyncBucket asyncBucket;

	public CouchbaseConnection(Config config)
	{
		cluster = CouchbaseCluster
				.create(config.getString(ServerConfigPropery.couchbaseNodes).split(","));

		bucket = cluster.openBucket(
				config.getString(ServerConfigPropery.couchbaseBucket),
				config.getString(ServerConfigPropery.couchbaseBucketPassword));

		asyncBucket = bucket.async();
	}

	public Bucket getBucket()
	{
		return bucket;
	}

	public AsyncBucket getAsyncBucket()
	{
		return asyncBucket;
	}

	public void shutdown()
	{
		asyncBucket.close();
		bucket.close();
		cluster.disconnect();
	}
}