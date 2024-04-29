package com.arcturus.appserver.system.maintainer.usecase.deployappscript;

import com.arcturus.appserver.net.NetStatusCode;

public class DeployAppScriptResponse
{
	NetStatusCode status;
	String msg;

	public DeployAppScriptResponse(NetStatusCode status, String msg)
	{
		this.status = status;
		this.msg = msg;
	}
}