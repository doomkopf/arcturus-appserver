package com.arcturus.appserver.system.account.login.dto;

public class ReconnectStatefulSessionRequest
{
	private String sId;

	private ReconnectStatefulSessionRequest()
	{
	}

	public ReconnectStatefulSessionRequest(String sId)
	{
		this.sId = sId;
	}

	public String getSessionId()
	{
		return sId;
	}
}