package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;
import com.arcturus.appserver.system.app.type.js.script.EntityUseCaseResultAction;
import com.arcturus.appserver.system.app.type.js.script.ScriptConstants;

import java.util.UUID;

/**
 * {@link EntityUseCaseHandler} for JS apps.
 *
 * @author doomkopf
 */
public class JsEntityUseCaseHandler implements EntityUseCaseHandler<JsonString>
{
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final String serviceName;
	private final String visibility;
	private final String createOrLoad;
	private final String useCaseId;
	private final boolean hasFunc;

	JsEntityUseCaseHandler(
		DomainAppScriptProvider domainAppScriptProvider,
		String serviceName,
		boolean isPublic,
		boolean isCreateEntity,
		String useCaseId,
		boolean hasFunc
	)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.serviceName = serviceName;
		visibility = isPublic ?
			ScriptConstants.ENTITY_USECASE_VISIBILITY_PUBLIC :
			ScriptConstants.ENTITY_USECASE_VISIBILITY_PRIVATE;
		createOrLoad = isCreateEntity ?
			ScriptConstants.ENTITY_USECASE_CREATEORLOAD_CREATE :
			ScriptConstants.ENTITY_USECASE_CREATEORLOAD_LOAD;
		this.useCaseId = useCaseId;
		this.hasFunc = hasFunc;
	}

	@Override
	public void handle(
		JsonString entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		String payload,
		UseCaseContext useCaseContext
	) throws ArcturusAppException
	{
		var entityUseCaseResult = domainAppScriptProvider.getAppScript().executeEntityUseCase(
			serviceName,
			visibility,
			createOrLoad,
			useCaseId,
			hasFunc,
			JsonString.getString(entity),
			id.toString(),
			Tools.encodeLongToRadix36String(requestId),
			(requestingUserId == null) ? null : requestingUserId.toString(),
			payload
		);

		if (entityUseCaseResult == null)
		{
			return;
		}

		if (entityUseCaseResult.action == EntityUseCaseResultAction.REMOVE)
		{
			useCaseContext.remove();
			return;
		}

		JsonString.handlePotentialStateChange(entity,
			entityUseCaseResult.newEntity,
			useCaseContext
		);
	}
}
