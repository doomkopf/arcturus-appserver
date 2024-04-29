package com.arcturus.appserver.system.maintainer;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.system.maintainer.entity.AppScriptEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class AppScriptStore
{
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;

	public AppScriptStore(StringKeyValueStore db, JsonStringSerializer jsonStringSerializer)
	{
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public Future<AppScriptEntity> getAppScript(String appId, Consumer<AppScriptEntity> consumer)
	{
		if (consumer == null)
		{
			var future = new CompletableFuture<AppScriptEntity>();
			db.asyncGet(DocumentKeys.appScript(appId),
				(k, v) -> future.complete(jsonStringSerializer.fromJsonString(AppScriptEntity.class,
					v
				))
			);

			return future;
		}

		db.asyncGet(DocumentKeys.appScript(appId),
			(k, v) -> consumer.accept(jsonStringSerializer.fromJsonString(AppScriptEntity.class, v))
		);

		return null;
	}

	public void storeAppScript(String appId, AppScriptEntity script, Runnable storedCallback)
	{
		db.asyncPut(DocumentKeys.appScript(appId),
			jsonStringSerializer.toJsonString(script),
			(key, putResult) -> storedCallback.run()
		);
	}
}