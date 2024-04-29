package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.service.UseCaseHandler;
import com.arcturus.appserver.system.app.service.UseCaseProvider;
import com.arcturus.appserver.system.app.service.UseCaseProvider.LateBindingUseCaseProvider;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.HashMap;
import java.util.Map;

public class JsUseCaseProvider implements UseCaseProvider, LateBindingUseCaseProvider
{
	private final Map<String, UseCaseHandler> idToUseCaseHandlerMap = new HashMap<>();

	public JsUseCaseProvider(
		DomainAppScriptProvider domainAppScriptProvider, JsServiceInfos serviceInfos
	)
	{
		var servicelessInfo = serviceInfos.getServicelessInfo();
		for (var useCase : servicelessInfo.getUseCasesIterable())
		{
			idToUseCaseHandlerMap.put(useCase.getId(), new JsUseCaseHandler(
				domainAppScriptProvider,
				useCase.getId(),
				useCase.isPublic(),
				serviceInfos.useCaseHasFunc("", useCase.getId())
			));
		}
	}

	@Override
	public void init()
	{
	}

	@Override
	public UseCaseHandler getUseCaseHandler(String useCaseId)
	{
		return idToUseCaseHandlerMap.get(useCaseId);
	}
}
