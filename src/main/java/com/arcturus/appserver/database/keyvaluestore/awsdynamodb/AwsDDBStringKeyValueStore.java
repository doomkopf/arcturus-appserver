package com.arcturus.appserver.database.keyvaluestore.awsdynamodb;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class AwsDDBStringKeyValueStore implements StringKeyValueStore
{
	private static final int MAX_VALUE_SIZE_PER_DOC = 320000;
	private static final String KEY_NEXT = "next";

	private final Logger log;

	private final String tableName;
	private final String keyName;
	private final String valueName;

	private final AmazonDynamoDBAsync ddbClient;

	public AwsDDBStringKeyValueStore(LoggerFactory loggerFactory, Config config)
	{
		this(loggerFactory,
			config.getString(ServerConfigPropery.awsDynamoDbAccessKey),
			config.getString(ServerConfigPropery.awsDynamoDbSecretKey),
			Regions.fromName(config.getString(ServerConfigPropery.awsDynamoDbRegion)),
			config.getString(ServerConfigPropery.awsDynamoDbTableName),
			config.getString(ServerConfigPropery.awsDynamoDbKeyName),
			config.getString(ServerConfigPropery.awsDynamoDbValueName)
		);
	}

	AwsDDBStringKeyValueStore(
		LoggerFactory loggerFactory,
		String accessKey,
		String secretKey,
		Regions region,
		String tableName,
		String keyName,
		String valueName
	)
	{
		log = loggerFactory.create(getClass());

		this.tableName = tableName;
		this.keyName = keyName;
		this.valueName = valueName;

		ddbClient = AmazonDynamoDBAsyncClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey,
				secretKey
			)))
			.withRegion(region)
			.build();
	}

	@Override
	public void asyncGet(
		String key, GetResultHandler<String, String> resultHandler
	)
	{
		recGet(key, "", key, resultHandler);
	}

	private void recGet(
		String chunkKey, String value, String key, GetResultHandler<String, String> resultHandler
	)
	{
		var request = new GetItemRequest(tableName,
			Collections.singletonMap(keyName, new AttributeValue(chunkKey))
		);
		ddbClient.getItemAsync(request, new AsyncHandler<>()
		{
			@Override
			public void onError(Exception e)
			{
				log.log(LogLevel.error, e);
				resultHandler.handle(key, null);
			}

			@Override
			public void onSuccess(
				GetItemRequest get, GetItemResult getItemResult
			)
			{
				try
				{
					var item = getItemResult.getItem();
					if (item == null)
					{
						resultHandler.handle(key, null);
						return;
					}

					var valueChunk = item.get(valueName).getS();
					var newValue = value + valueChunk;
					var next = item.get(KEY_NEXT);
					if (next == null)
					{
						resultHandler.handle(key, newValue);
						return;
					}

					recGet(next.getS(), newValue, key, resultHandler);
				}
				catch (Exception e)
				{
					log.log(LogLevel.error, e);
					resultHandler.handle(key, null);
				}
			}
		});
	}

	@Override
	public void asyncPut(
		String key, String value, PutResultHandler<String> resultHandler
	)
	{
		try
		{
			var putRequests = buildPutRequests(key, value);
			recPut(0, putRequests, key, resultHandler);
		}
		catch (Exception e)
		{
			log.log(LogLevel.error, e);
			resultHandler.handle(key, PutResult.unknownError);
		}
	}

	private List<PutItemRequest> buildPutRequests(String key, String value)
	{
		var chunks = value.length() / MAX_VALUE_SIZE_PER_DOC;
		if ((value.length() % MAX_VALUE_SIZE_PER_DOC) > 0)
		{
			chunks++;
		}
		var putRequests = new ArrayList<PutItemRequest>(chunks);
		var i = 0;
		for (var chunk = 0; chunk < chunks; chunk++)
		{
			var remaining = Math.min(MAX_VALUE_SIZE_PER_DOC, value.length() - i);
			var valueChunk = value.substring(i, i + remaining);
			i += remaining;

			var item = new TreeMap<String, AttributeValue>();
			item.put(valueName, new AttributeValue(valueChunk));

			item.put(keyName, new AttributeValue(key + (chunk == 0 ? "" : ("_" + (chunk - 1)))));

			if ((chunk + 1) != chunks)
			{
				item.put(KEY_NEXT, new AttributeValue(key + '_' + chunk));
			}

			putRequests.add(new PutItemRequest(tableName, item));
		}

		return putRequests;
	}

	private void recPut(
		int index,
		List<PutItemRequest> putRequests,
		String key,
		PutResultHandler<String> resultHandler
	)
	{
		ddbClient.putItemAsync(putRequests.get(index), new AsyncHandler<>()
		{
			@Override
			public void onError(Exception e)
			{
				log.log(LogLevel.error, e);
				resultHandler.handle(key, PutResult.unknownError);
			}

			@Override
			public void onSuccess(
				PutItemRequest put, PutItemResult putItemResult
			)
			{
				var nextIndex = index + 1;
				if (nextIndex == putRequests.size())
				{
					resultHandler.handle(key, PutResult.ok);
				}
				else
				{
					recPut(nextIndex, putRequests, key, resultHandler);
				}
			}
		});
	}

	@Override
	public void asyncRemove(
		String key, RemoveResultHandler<String> resultHandler
	)
	{
		var request = new DeleteItemRequest(tableName,
			Collections.singletonMap(keyName, new AttributeValue(key))
		);
		ddbClient.deleteItemAsync(request, new AsyncHandler<>()
		{
			@Override
			public void onError(Exception e)
			{
				log.log(LogLevel.error, e);
				resultHandler.handle(key, RemoveResult.unknownError);
			}

			@Override
			public void onSuccess(
				DeleteItemRequest delete, DeleteItemResult deleteItemResult
			)
			{
				resultHandler.handle(key, RemoveResult.ok);
			}
		});
	}

	@Override
	public void shutdown()
	{
		ddbClient.shutdown();
	}
}