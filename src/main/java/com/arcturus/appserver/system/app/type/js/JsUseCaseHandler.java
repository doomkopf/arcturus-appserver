package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCaseHandler;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;
import com.arcturus.appserver.system.app.type.js.script.ScriptConstants;

import java.util.UUID;

public class JsUseCaseHandler implements UseCaseHandler
{
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final String visibility;
	private final String useCaseId;
	private final boolean hasFunc;

	JsUseCaseHandler(
		DomainAppScriptProvider domainAppScriptProvider,
		String useCaseId,
		boolean isPublic,
		boolean hasFunc
	)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		visibility = isPublic ?
			ScriptConstants.ENTITY_USECASE_VISIBILITY_PUBLIC :
			ScriptConstants.ENTITY_USECASE_VISIBILITY_PRIVATE;
		this.useCaseId = useCaseId;
		this.hasFunc = hasFunc;
	}

	@Override
	public void handle(
		long requestId, UUID requestingUserId, String payload, RequestInfo requestInfo
	) throws ArcturusAppException
	{
		domainAppScriptProvider.getAppScript().executeUseCase(
			visibility,
			useCaseId,
			hasFunc,
			Tools.encodeLongToRadix36String(requestId),
			(requestingUserId == null) ? null : requestingUserId.toString(),
			payload,
			(requestInfo == null) ? null : ("{\"ip\":\"" + requestInfo.getIp() + "\"}")
		);
	}
}
