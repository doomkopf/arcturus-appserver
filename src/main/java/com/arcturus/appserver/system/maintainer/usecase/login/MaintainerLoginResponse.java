package com.arcturus.appserver.system.maintainer.usecase.login;

import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.InternalUseCases;

class MaintainerLoginResponse
{
	String uc = InternalUseCases.MAINTAINER_LOGIN;
	NetStatusCode status;
	String sId;

	MaintainerLoginResponse(NetStatusCode status, String sId)
	{
		this.status = status;
		this.sId = sId;
	}

	MaintainerLoginResponse(NetStatusCode status)
	{
		this(status, null);
	}
}