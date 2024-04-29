package com.arcturus.appserver.inject.spring;

import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.KeyValueStoreDatabaseType;
import com.arcturus.appserver.config.ServerConfigPropery;

/**
 * Wraps {@link Config} to have concrete/parameterless methods for spring xml
 * config.
 * 
 * @author doomkopf
 */
public class ConfigWrapper
{
	private final Config config;

	public ConfigWrapper(Config config)
	{
		this.config = config;
	}

	public boolean doCouchbaseConnection()
	{
		return config.getEnum(
				KeyValueStoreDatabaseType.class,
				ServerConfigPropery.keyValueStoreDatabaseType) == KeyValueStoreDatabaseType.couchbase;
	}
}