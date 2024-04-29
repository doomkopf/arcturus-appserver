package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;
import com.arcturus.api.service.entity.transaction.ValidationResult;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.UUID;

public class JsEntityTransactionUseCaseHandler
	implements EntityTransactionUseCaseHandler<JsonString>
{
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final String serviceName;
	private final String useCaseId;
	private final JsonFactory jsonFactory;

	JsEntityTransactionUseCaseHandler(
		DomainAppScriptProvider domainAppScriptProvider,
		String serviceName,
		String useCaseId,
		JsonFactory jsonFactory
	)
	{
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.serviceName = serviceName;
		this.useCaseId = useCaseId;
		this.jsonFactory = jsonFactory;
	}

	@Override
	public ValidationResult validate(
		JsonString entity, UUID id, long requestId, UUID requestingUserId, String payload
	) throws ArcturusAppException
	{
		var returnedObject = domainAppScriptProvider.getAppScript()
			.executeEntityTransactionUseCaseValidate(
				serviceName,
				useCaseId,
				entity.getString(),
				id.toString(),
				Tools.encodeLongToRadix36String(requestId),
				requestingUserId.toString(),
				payload
			);

		var json = jsonFactory.parseReadonly(returnedObject);

		var ok = json.getBool("ok").booleanValue();
		String resultPayload = null;
		if (json.has("payload"))
		{
			resultPayload = json.getObject("payload").toString();
		}

		return new ValidationResult(ok, resultPayload);
	}

	@Override
	public void commit(
		JsonString entity, UUID id, long requestId, UUID requestingUserId, String payload
	) throws ArcturusAppException
	{
		var newEntity = domainAppScriptProvider.getAppScript()
			.executeEntityTransactionUseCaseCommit(
				serviceName,
				useCaseId,
				JsonString.getString(entity),
				id.toString(),
				Tools.encodeLongToRadix36String(requestId),
				requestingUserId.toString(),
				payload
			);

		JsonString.handlePotentialStateChange(entity, newEntity, null);
	}
}
