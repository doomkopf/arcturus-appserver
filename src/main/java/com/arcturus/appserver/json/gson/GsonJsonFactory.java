package com.arcturus.appserver.json.gson;

import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.json.JsonObject;
import com.arcturus.appserver.json.ReadonlyJsonObject;
import com.google.gson.JsonParser;

/**
 * @author doomkopf
 */
public class GsonJsonFactory implements JsonFactory
{
	@Override
	public JsonObject create()
	{
		return new GsonJsonObject();
	}

	@Override
	public JsonObject parse(String json)
	{
		return new GsonJsonObject(JsonParser.parseString(json).getAsJsonObject());
	}

	@Override
	public ReadonlyJsonObject parseReadonly(String json)
	{
		return parse(json);
	}
}