package com.arcturus.appserver.api;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.AppManager;

/**
 * The API that is returned when using arcturus embedded.
 *
 * @author doomkopf
 */
public class ArcturusApi
{
	private final AppManager appManager;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusApi(AppManager appManager, JsonStringSerializer jsonStringSerializer)
	{
		this.appManager = appManager;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public ArcturusApp getApp(String appId)
	{
		var app = appManager.getApp(appId);
		if (app == null)
		{
			return null;
		}

		return new ArcturusApp(new ArcturusUseCaseHandler(app, jsonStringSerializer));
	}
}