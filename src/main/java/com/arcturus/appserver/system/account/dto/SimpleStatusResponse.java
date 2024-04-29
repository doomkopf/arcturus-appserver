package com.arcturus.appserver.system.account.dto;

import com.arcturus.appserver.net.NetStatusCode;

public class SimpleStatusResponse
{
	String uc;
	NetStatusCode status;

	public SimpleStatusResponse(String uc, NetStatusCode status)
	{
		this.uc = uc;
		this.status = status;
	}
}