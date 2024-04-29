package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.entity.EntityUpdater;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.UUID;

public class JsEntityUpdater implements EntityUpdater<JsonString>
{
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final String serviceName;

	JsEntityUpdater(DomainAppScriptProvider domainAppScriptProvider, String serviceName)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.serviceName = serviceName;
	}

	@Override
	public void update(
		JsonString entity,
		UUID id,
		long currentTimeMillis,
		long deltaTime,
		UseCaseContext useCaseContext
	) throws ArcturusAppException
	{
		var newEntity = domainAppScriptProvider.getAppScript()
			.executeUpdate(serviceName,
				JsonString.getString(entity),
				id.toString(),
				currentTimeMillis,
				deltaTime
			);

		JsonString.handlePotentialStateChange(entity, newEntity, useCaseContext);
	}
}