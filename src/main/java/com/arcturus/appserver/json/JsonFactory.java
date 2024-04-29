package com.arcturus.appserver.json;

/**
 * Creates json objects.
 * 
 * @author doomkopf
 */
public interface JsonFactory
{
	JsonObject create();

	JsonObject parse(String json);

	ReadonlyJsonObject parseReadonly(String json);
}