package com.arcturus.appserver.json.gson;

import com.arcturus.appserver.json.JsonObject;

import java.util.Iterator;

/**
 * @author doomkopf
 */
public class GsonJsonObject implements JsonObject
{
	public final com.google.gson.JsonObject json;

	public GsonJsonObject()
	{
		this(new com.google.gson.JsonObject());
	}

	GsonJsonObject(com.google.gson.JsonObject json)
	{
		this.json = json;
	}

	@Override
	public boolean has(String name)
	{
		return json.has(name);
	}

	@Override
	public boolean isObject(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return false;
		}

		return jsonElement.isJsonObject();
	}

	@Override
	public boolean isArray(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return false;
		}

		return jsonElement.isJsonArray();
	}

	@Override
	public Integer getInt(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null;
		}

		return Integer.valueOf(jsonElement.getAsInt());
	}

	@Override
	public Long getLong(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null;
		}

		return Long.valueOf(jsonElement.getAsLong());
	}

	@Override
	public Boolean getBool(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null; // NOSONAR it's for json parsing, not for boolean logic
		}

		return Boolean.valueOf(jsonElement.getAsBoolean());
	}

	@Override
	public String getString(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null;
		}

		return jsonElement.getAsString();
	}

	@Override
	public GsonJsonObject getObject(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null;
		}

		return new GsonJsonObject(jsonElement.getAsJsonObject());
	}

	@Override
	public GsonJsonArray getArray(String name)
	{
		var jsonElement = json.get(name);
		if (jsonElement == null)
		{
			return null;
		}

		return new GsonJsonArray(jsonElement.getAsJsonArray());
	}

	@Override
	public GsonJsonObject setInt(String name, Integer value)
	{
		json.addProperty(name, value);
		return this;
	}

	@Override
	public GsonJsonObject setLong(String name, Long value)
	{
		json.addProperty(name, value);
		return this;
	}

	@Override
	public GsonJsonObject setBool(String name, Boolean value)
	{
		json.addProperty(name, value);
		return this;
	}

	@Override
	public GsonJsonObject setString(String name, String value)
	{
		json.addProperty(name, value);
		return this;
	}

	@Override
	public JsonObject setObject(String name, JsonObject object)
	{
		json.add(name, ((GsonJsonObject) object).json);
		return this;
	}

	@Override
	public GsonJsonObject createObject(String name)
	{
		var obj = new com.google.gson.JsonObject();
		json.add(name, obj);

		return new GsonJsonObject(obj);
	}

	@Override
	public GsonJsonArray createArray(String name)
	{
		var array = new com.google.gson.JsonArray();
		json.add(name, array);

		return new GsonJsonArray(array);
	}

	@Override
	public Iterator<String> iterator()
	{
		return json.keySet().iterator();
	}

	@Override
	public String toString()
	{
		return json.toString();
	}
}