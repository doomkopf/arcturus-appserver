package com.arcturus.appserver.json;

/**
 * @author doomkopf
 */
public interface JsonObject extends ReadonlyJsonObject
{
	JsonObject setInt(String name, Integer value);

	JsonObject setLong(String name, Long value);

	JsonObject setBool(String name, Boolean value);

	JsonObject setString(String name, String value);

	JsonObject setObject(String name, JsonObject object);

	JsonObject createObject(String name);

	JsonArray createArray(String name);
}