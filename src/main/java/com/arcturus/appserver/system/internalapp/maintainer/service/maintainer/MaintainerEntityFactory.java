package com.arcturus.appserver.system.internalapp.maintainer.service.maintainer;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.type.java.JavaEntityFactory;

import java.util.ArrayList;
import java.util.UUID;

@ServiceAssignment(service = ArcturusEntityService.SERVICE_NAME_USER)
public class MaintainerEntityFactory implements JavaEntityFactory<Maintainer>
{
	@Override
	public Maintainer createDefaultEntity(UUID id)
	{
		return new Maintainer(new ArrayList<>(1));
	}

	@Override
	public int getCurrentVersion()
	{
		return 1;
	}
}