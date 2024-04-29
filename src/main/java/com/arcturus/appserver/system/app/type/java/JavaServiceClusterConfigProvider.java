package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.tool.FileReader;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.system.app.inject.AppId;
import com.arcturus.appserver.system.app.service.ServiceClusterConfig;
import com.arcturus.appserver.system.app.service.ServiceClusterConfigProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides {@link ServiceClusterConfig}s by their name.
 *
 * @author doomkopf
 */
public class JavaServiceClusterConfigProvider implements ServiceClusterConfigProvider
{
	private final Map<String, ServiceClusterConfig> nameToConfigMap = new HashMap<>();

	public JavaServiceClusterConfigProvider(
		@AppId
			String appId, FileReader fileReader
	) throws ArcturusAppException
	{
		List<String> lines;
		try
		{
			lines = fileReader.readExclusiveFileLines(Config.PATH_CONFIG + "/" + appId + ".conf");
		}
		catch (IOException e)
		{
			return;
		}

		for (var line : lines)
		{
			var isIncluding = true;
			var nameConfig = line.split("\\+", 2);
			if (nameConfig.length == 1)
			{
				nameConfig = line.split("-", 2);
				if (nameConfig.length == 1)
				{
					throw new ArcturusAppException("Syntax error in cluster config");
				}
				isIncluding = false;
			}

			var name = nameConfig[0].trim();
			var nodes = nameConfig[1].split(",");
			for (var i = 0; i < nodes.length; i++)
			{
				nodes[i] = nodes[i].trim();
			}

			nameToConfigMap.put(name, new ServiceClusterConfig(isIncluding, nodes));
		}
	}

	@Override
	public ServiceClusterConfig getByName(String serviceName)
	{
		var config = nameToConfigMap.get(serviceName);
		if (config == null)
		{
			return DEFAULT_CONFIG;
		}

		return config;
	}
}