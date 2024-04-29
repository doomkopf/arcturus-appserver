package com.arcturus.appserver.system.account.login.dto;

import com.arcturus.appserver.net.NetStatusCode;

public class GetEmailResponse
{
	String uc;
	NetStatusCode status;
	String email;

	public GetEmailResponse(String uc, NetStatusCode status, String email)
	{
		this.uc = uc;
		this.status = status;
		this.email = email;
	}
}