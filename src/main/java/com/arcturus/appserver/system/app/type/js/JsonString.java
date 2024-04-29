package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.service.entity.UseCaseContext;

public class JsonString
{
	static String getString(JsonString jsonString)
	{
		if (jsonString == null)
		{
			return null;
		}

		return jsonString.json;
	}

	static void handlePotentialStateChange(
		JsonString entity, String newEntity, UseCaseContext useCaseContext
	)
	{
		if ((entity != null) && (newEntity != null))
		{
			entity.json = newEntity;
			if (useCaseContext != null)
			{
				useCaseContext.dirty();
			}
		}
	}

	private String json;

	JsonString(String json)
	{
		this.json = json;
	}

	public String getString()
	{
		return json;
	}

	public void setString(String json)
	{
		this.json = json;
	}
}