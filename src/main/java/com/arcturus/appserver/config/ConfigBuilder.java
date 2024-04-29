package com.arcturus.appserver.config;

import java.util.HashMap;
import java.util.Map;

public final class ConfigBuilder
{
	private final Map<String, String> map = new HashMap<>();

	private ConfigBuilder()
	{
	}

	public static ConfigBuilder empty()
	{
		return new ConfigBuilder();
	}

	public ConfigBuilder config(ConfigProperty configProperty, String value)
	{
		map.put(configProperty.name(), value);
		return this;
	}

	public ConfigBuilder config(ConfigProperty configProperty, int value)
	{
		return config(configProperty, String.valueOf(value));
	}

	public ConfigBuilder config(ConfigProperty configProperty, boolean value)
	{
		return config(configProperty, String.valueOf(value));
	}

	public ConfigBuilder config(ConfigProperty configProperty, Enum<?> value)
	{
		return config(configProperty, value.name());
	}

	public Config build()
	{
		return new Config(map);
	}
}