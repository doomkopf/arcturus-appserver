package com.arcturus.appserver.system.maintainer.entity;

import java.util.UUID;

public class AppScriptEntity
{
	private String script;
	private UUID maintainerUserId;

	@SuppressWarnings("unused")
	private AppScriptEntity()
	{
	}

	public AppScriptEntity(String script, UUID maintainerUserId)
	{
		this.script = script;
		this.maintainerUserId = maintainerUserId;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public UUID getMaintainerUserId()
	{
		return maintainerUserId;
	}
}