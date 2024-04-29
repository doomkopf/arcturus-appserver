package com.arcturus.appserver.json;

/**
 * @author doomkopf
 */
public interface JsonArray extends ReadonlyJsonArray
{
	JsonArray addInt(Integer value);

	JsonArray addLong(Long value);

	JsonArray addBool(Boolean value);

	JsonArray addString(String value);

	JsonObject createObject();

	JsonArray createArray();
}