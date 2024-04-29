package com.arcturus.appserver.system.internalapp.maintainer.usecase.getapps;

import java.util.List;

import com.arcturus.appserver.system.internalapp.maintainer.dto.MaintainerAppDto;

class GetAppsResponse
{
	List<MaintainerAppDto> apps;

	GetAppsResponse(List<MaintainerAppDto> apps)
	{
		this.apps = apps;
	}
}