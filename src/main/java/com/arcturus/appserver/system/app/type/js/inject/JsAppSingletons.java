package com.arcturus.appserver.system.app.type.js.inject;

import java.util.UUID;

public class JsAppSingletons
{
	private final UUID maintainerUserId;
	private final String appScriptCode;

	public JsAppSingletons(UUID maintainerUserId, String appScriptCode)
	{
		this.maintainerUserId = maintainerUserId;
		this.appScriptCode = appScriptCode;
	}

	public UUID getMaintainerUserId()
	{
		return maintainerUserId;
	}

	public String getAppScriptCode()
	{
		return appScriptCode;
	}
}