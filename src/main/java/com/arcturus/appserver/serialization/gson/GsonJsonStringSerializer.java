package com.arcturus.appserver.serialization.gson;

import com.arcturus.api.tool.JsonStringSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.bind.MapTypeAdapterFactory;

import java.lang.reflect.Type;
import java.util.*;

/**
 * GSON based implementation of {@link JsonStringSerializer}.
 *
 * @author doomkopf
 */
public class GsonJsonStringSerializer implements JsonStringSerializer
{
	private final Gson gson;

	public GsonJsonStringSerializer()
	{
		var instanceCreators = new HashMap<Type, InstanceCreator<?>>();

		instanceCreators.put(Collection.class, type -> new LinkedList<>());

		instanceCreators.put(List.class, type -> new ArrayList<>(0));

		instanceCreators.put(Map.class, type -> new HashMap<>());

		instanceCreators.put(Set.class, type -> new HashSet<>());

		gson = new GsonBuilder().registerTypeAdapterFactory(new MapTypeAdapterFactory(new ConstructorConstructor(
			instanceCreators), false)).create();
	}

	@Override
	public String toJsonString(Object obj)
	{
		return gson.toJson(obj);
	}

	@Override
	public <T> T fromJsonString(Type type, String json)
	{
		return gson.fromJson(json, type);
	}
}