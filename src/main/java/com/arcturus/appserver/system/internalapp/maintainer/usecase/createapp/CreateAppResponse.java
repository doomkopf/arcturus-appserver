package com.arcturus.appserver.system.internalapp.maintainer.usecase.createapp;

import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.internalapp.maintainer.dto.MaintainerAppDto;

class CreateAppResponse
{
	NetStatusCode status;
	MaintainerAppDto app;

	CreateAppResponse(NetStatusCode status, MaintainerAppDto app)
	{
		this.status = status;
		this.app = app;
	}
}