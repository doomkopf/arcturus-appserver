package com.arcturus.appserver.system.maintainer.usecase.getappscript;

import com.arcturus.appserver.net.NetStatusCode;

public class GetAppScriptResponse
{
	NetStatusCode status = NetStatusCode.ok;
	String appId;
	String script;

	public GetAppScriptResponse(String appId, String script)
	{
		this.appId = appId;
		this.script = script;
	}
}