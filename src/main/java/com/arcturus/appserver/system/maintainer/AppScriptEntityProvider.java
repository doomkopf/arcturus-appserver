package com.arcturus.appserver.system.maintainer;

import com.arcturus.api.tool.FileReader;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.maintainer.entity.AppScriptEntity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class AppScriptEntityProvider
{
	private static final String TEST_APP_ID = "test_js";
	private static final String TEST_APP_PATH = "js/test/app/app.js";

	private static final String FILES_CONF = "/files.conf";
	private static final char PATH_DELIMITER = '/';

	private final FileReader fileReader;
	private final Config config;
	private final AppScriptStore appScriptStore;

	public AppScriptEntityProvider(
		FileReader fileReader, Config config, AppScriptStore appScriptStore
	)
	{
		this.fileReader = fileReader;
		this.config = config;
		this.appScriptStore = appScriptStore;
	}

	public Future<AppScriptEntity> getAppScript(String appId, Consumer<AppScriptEntity> consumer)
	{
		AppScriptEntity appScript;

		if (appId.equals(TEST_APP_ID))
		{
			try
			{
				appScript = new AppScriptEntity(fileReader.readResourcesFile(TEST_APP_PATH),
					Constants.ZERO_UUID
				);
			}
			catch (IOException e)
			{
				appScript = null;
			}
		}
		else
		{
			return appScriptStore.getAppScript(appId, consumer);
		}

		if (consumer == null)
		{
			var completableFuture = new CompletableFuture<AppScriptEntity>();
			completableFuture.complete(appScript);
			return completableFuture;
		}

		consumer.accept(appScript);

		return null;
	}

	private String getFromAppsPath(String appId) throws IOException
	{
		var appsPath = config.getString(ServerConfigPropery.appsPath) + PATH_DELIMITER + appId;

		var files = fileReader.readResourcesFile(appsPath + FILES_CONF)
			.split(System.getProperty("line.separator"));

		var strAppScript = new StringBuilder();
		for (var file : files)
		{
			strAppScript.append(fileReader.readResourcesFile(appsPath + PATH_DELIMITER + file));
		}

		return strAppScript.toString();
	}
}