package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.entity.EntityFactory;
import com.arcturus.api.service.entity.EntityMigration;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.app.service.entity.EntityInitializer;
import com.arcturus.appserver.system.app.service.entity.EntityMigrator;
import com.arcturus.appserver.system.app.service.entity.EntitySerializer;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.UUID;

/**
 * Implements a bunch of entity management interfaces to make them call the
 * respective JS function.
 *
 * @author doomkopf
 */
public class JsEntityManagement
	implements EntityFactory<JsonString>, EntitySerializer<JsonString>, EntityMigration,
	EntityInitializer<JsonString>
{
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final JsonFactory jsonFactory;
	private final String serviceName;

	JsEntityManagement(
		DomainAppScriptProvider domainAppScriptProvider, JsonFactory jsonFactory, String serviceName
	)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.jsonFactory = jsonFactory;
		this.serviceName = serviceName;
	}

	@Override
	public JsonString createDefaultEntity(UUID id) throws ArcturusAppException
	{
		return new JsonString(domainAppScriptProvider.getAppScript()
			.createDefaultEntity(serviceName, id.toString()));
	}

	@Override
	public String entityToString(JsonString entity, int version)
	{
		var json = jsonFactory.parse(entity.getString());
		json.setInt(EntityMigrator.JSON_KEY_VERSION, version);
		return json.toString();
	}

	@Override
	public JsonString entityFromString(String str)
	{
		return new JsonString(str);
	}

	@Override
	public JsonString initializeEntity(JsonString entity) throws ArcturusAppException
	{
		entity.setString(domainAppScriptProvider.getAppScript()
			.initEntity(serviceName, entity.getString()));
		return entity;
	}

	@Override
	public String migrate(String entityJsonString, int toVersion) throws ArcturusAppException
	{
		return domainAppScriptProvider.getAppScript()
			.migrateToV(serviceName, toVersion, entityJsonString);
	}
}