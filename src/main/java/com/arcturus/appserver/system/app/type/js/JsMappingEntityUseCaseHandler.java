package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.entity.aggregation.MappingEntityUseCaseHandler;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.UUID;

public class JsMappingEntityUseCaseHandler implements MappingEntityUseCaseHandler<JsonString>
{
	private final DomainAppScriptProvider domainAppScriptProvider;

	private final String serviceName;
	private final String functionName;

	JsMappingEntityUseCaseHandler(
		DomainAppScriptProvider domainAppScriptProvider, String serviceName, String functionName
	)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.serviceName = serviceName;
		this.functionName = functionName;
	}

	@Override
	public String map(JsonString entity, UUID id) throws ArcturusAppException
	{
		return domainAppScriptProvider.getAppScript()
			.executeEntityMapper(serviceName, functionName, entity.getString(), id.toString());
	}
}