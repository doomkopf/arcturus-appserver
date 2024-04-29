package com.arcturus.appserver.system.maintainer.usecase.register;

import com.arcturus.appserver.net.NetStatusCode;

class MaintainerRegistrationResponse
{
	NetStatusCode status;

	MaintainerRegistrationResponse(NetStatusCode status)
	{
		this.status = status;
	}
}