package com.arcturus.appserver.json.gson;

import com.arcturus.appserver.json.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * @author doomkopf
 */
public class GsonJsonArray implements JsonArray
{
	final com.google.gson.JsonArray json;

	public GsonJsonArray()
	{
		json = new com.google.gson.JsonArray();
	}

	public GsonJsonArray(com.google.gson.JsonArray json)
	{
		this.json = json;
	}

	@Override
	public int length()
	{
		return json.size();
	}

	@Override
	public Integer getInt(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null;
		}

		return Integer.valueOf(jsonElement.getAsInt());
	}

	@Override
	public Long getLong(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null;
		}

		return Long.valueOf(jsonElement.getAsLong());
	}

	@Override
	public Boolean getBool(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null; // NOSONAR it's for json parsing, not for boolean logic
		}

		return Boolean.valueOf(jsonElement.getAsBoolean());
	}

	@Override
	public String getString(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null;
		}

		return jsonElement.getAsString();
	}

	@Override
	public GsonJsonObject getObject(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null;
		}

		return new GsonJsonObject(jsonElement.getAsJsonObject());
	}

	@Override
	public boolean isObject(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return false;
		}

		return jsonElement.isJsonObject();
	}

	@Override
	public GsonJsonArray getArray(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return null;
		}

		return new GsonJsonArray(jsonElement.getAsJsonArray());
	}

	@Override
	public boolean isArray(int index)
	{
		var jsonElement = json.get(index);
		if (jsonElement == null)
		{
			return false;
		}

		return jsonElement.isJsonArray();
	}

	@Override
	public GsonJsonArray addInt(Integer value)
	{
		json.add(new JsonPrimitive(value));
		return this;
	}

	@Override
	public GsonJsonArray addLong(Long value)
	{
		json.add(new JsonPrimitive(value));
		return this;
	}

	@Override
	public GsonJsonArray addBool(Boolean value)
	{
		json.add(new JsonPrimitive(value));
		return this;
	}

	@Override
	public GsonJsonArray addString(String value)
	{
		json.add(new JsonPrimitive(value));
		return this;
	}

	@Override
	public GsonJsonObject createObject()
	{
		var obj = new com.google.gson.JsonObject();
		json.add(obj);

		return new GsonJsonObject(obj);
	}

	@Override
	public GsonJsonArray createArray()
	{
		var array = new com.google.gson.JsonArray();
		json.add(array);

		return new GsonJsonArray(array);
	}

	@Override
	public String toString()
	{
		return json.toString();
	}
}