package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityMigration;
import com.arcturus.appserver.json.JsonFactory;

/**
 * Migrates an entity to the latest version (if not already) before it is passed
 * on to the app layer.
 *
 * @author doomkopf
 */
public class EntityMigrator
{
	public static final String JSON_KEY_VERSION = "_iv";

	private final Logger log;
	private final int currentVersion;
	private final EntityMigration entityMigration;
	private final JsonFactory jsonFactory;

	public EntityMigrator(
		LoggerFactory loggerFactory,
		int currentVersion,
		EntityMigration entityMigration,
		JsonFactory jsonFactory
	)
	{
		log = loggerFactory.create(getClass());
		this.currentVersion = currentVersion;
		this.entityMigration = entityMigration;
		this.jsonFactory = jsonFactory;
	}

	String migrateEntityIfNecessary(String entityJsonString)
	{
		var entityJson = jsonFactory.parse(entityJsonString);

		var versionInteger = entityJson.getInt(JSON_KEY_VERSION);
		if (versionInteger == null)
		{
			log.log(LogLevel.info, "Entity has no version property");
			return null;
		}

		var version = versionInteger.intValue();

		if (version > currentVersion)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Entity version bigger than the current one");
			}
			return null;
		}

		if (version == currentVersion)
		{
			return entityJsonString;
		}

		for (version++; version <= currentVersion; version++)
		{
			try
			{
				entityJsonString = entityMigration.migrate(entityJsonString, version);
			}
			catch (ArcturusAppException e)
			{
				if (log.isLogLevel(LogLevel.error))
				{
					log.log(LogLevel.error, e);
				}

				return null;
			}
		}

		return entityJsonString;
	}
}