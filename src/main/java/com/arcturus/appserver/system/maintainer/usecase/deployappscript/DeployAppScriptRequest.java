package com.arcturus.appserver.system.maintainer.usecase.deployappscript;

public class DeployAppScriptRequest
{
	private String appId;
	private String script;

	public String getAppId()
	{
		return appId;
	}

	public String getScript()
	{
		return script;
	}
}