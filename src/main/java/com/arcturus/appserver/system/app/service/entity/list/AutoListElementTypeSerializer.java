package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.api.tool.JsonStringSerializer;

public class AutoListElementTypeSerializer<T> implements ListElementTypeSerializer<T>
{
	private final Class<T> clazz;
	private final JsonStringSerializer jsonStringSerializer;

	public AutoListElementTypeSerializer(
		Class<T> clazz, JsonStringSerializer jsonStringSerializer
	)
	{
		this.clazz = clazz;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public String elementToString(T elem)
	{
		return jsonStringSerializer.toJsonString(elem);
	}

	@Override
	public T elementFromString(String str)
	{
		return jsonStringSerializer.fromJsonString(clazz, str);
	}
}