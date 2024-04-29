package com.arcturus.appserver.database.keyvaluestore;

import java.util.concurrent.ExecutorService;

import com.arcturus.api.LoggerFactory;

/**
 * Another class just for type safety/compatibility.
 * 
 * @author doomkopf
 */
public class StringAsyncWrapper extends AsyncWrapper<String, String> implements StringKeyValueStore
{
	public StringAsyncWrapper(
			LoggerFactory loggerFactory,
			SyncKeyValueStore<String, String> syncStore,
			ExecutorService executorService)
	{
		super(loggerFactory, syncStore, executorService);
	}
}