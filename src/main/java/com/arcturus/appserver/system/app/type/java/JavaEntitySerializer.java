package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.EntityMigrator;
import com.arcturus.appserver.system.app.service.entity.EntitySerializer;

import java.lang.reflect.Type;

/**
 * Java class based implementation of {@link EntitySerializer}.
 *
 * @author doomkopf
 */
public class JavaEntitySerializer<E> implements EntitySerializer<E>
{
	private final JsonStringSerializer jsonStringSerializer;
	private final Type entityType;

	public JavaEntitySerializer(
		JsonStringSerializer jsonStringSerializer, Type entityType
	)
	{
		this.jsonStringSerializer = jsonStringSerializer;
		this.entityType = entityType;
	}

	@Override
	public String entityToString(E entity, int version)
	{
		var jsonString = jsonStringSerializer.toJsonString(entity);
		jsonString = jsonString.substring(0, jsonString.length() - 1);
		jsonString += ",\"" + EntityMigrator.JSON_KEY_VERSION + "\":" + version + '}';
		return jsonString;
	}

	@Override
	public E entityFromString(String str)
	{
		return jsonStringSerializer.fromJsonString(entityType, str);
	}
}