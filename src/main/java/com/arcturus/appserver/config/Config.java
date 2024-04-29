package com.arcturus.appserver.config;

import com.arcturus.appserver.system.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All config properties.
 *
 * @author doomkopf
 */
public class Config
{
	public static final String PATH_CONFIG = "config";

	private static final String PATH = PATH_CONFIG + "/arcturus.cfg";

	private final Map<String, String> configMap;

	public Config()
	{
		configMap = new ConcurrentHashMap<>();

		var fileLines = readExclusiveFile();
		for (var line : fileLines)
		{
			var keyValue = line.split("=");
			configMap.put(keyValue[0], keyValue[1]);
		}
	}

	public Config(Map<String, String> configMap)
	{
		this.configMap = new ConcurrentHashMap<>(configMap);
	}

	public String getString(ConfigProperty configProperty)
	{
		var value = configMap.get(configProperty.name());
		if (value == null)
		{
			return configProperty.defaultValue();
		}

		return value;
	}

	public int getInt(ConfigProperty configProperty)
	{
		var value = getString(configProperty);
		if (value == null)
		{
			return 0;
		}

		return Integer.parseInt(value);
	}

	public boolean getBool(ConfigProperty configProperty)
	{
		var value = getString(configProperty);
		return Boolean.parseBoolean(value);
	}

	public <T extends Enum<T>> T getEnum(Class<T> enumType, ConfigProperty configProperty)
	{
		var value = getString(configProperty);
		if (value == null)
		{
			return null;
		}

		return Enum.valueOf(enumType, value);
	}

	private void set(ConfigProperty configProperty, String value)
	{
		configMap.put(configProperty.name(), value);
		writeToFile();
	}

	public void set(ConfigProperty configProperty, int value)
	{
		set(configProperty, String.valueOf(value));
	}

	public void set(ConfigProperty configProperty, boolean value)
	{
		set(configProperty, String.valueOf(value));
	}

	private static List<String> readExclusiveFile()
	{
		try
		{
			return Files.readAllLines(Paths.get(PATH));
		}
		catch (Throwable e)
		{
			return Collections.emptyList();
		}
	}

	private void writeToFile()
	{
		var sb = new StringBuilder();
		for (var entry : configMap.entrySet())
		{
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
			sb.append(System.lineSeparator());
		}

		try
		{
			Files.write(Paths.get(PATH), sb.toString().getBytes(Constants.CHARSET_UTF8));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}