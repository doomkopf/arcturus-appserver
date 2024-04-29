package com.arcturus.appserver.net;

import com.arcturus.appserver.json.gson.GsonJsonObject;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.ArcturusUserSender;

import java.util.UUID;

public interface NetCodes // NOSONAR
{
	String JSON_KEY_STATUS = "status";

	String OK_JSON = new GsonJsonObject().setString(JSON_KEY_STATUS, NetStatusCode.ok.name())
		.toString();

	String ERROR_JSON_INTERNAL_ERROR = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.internalError.name()
	).toString();

	String ERROR_JSON_INVALID_REQUEST = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.invalidRequest.name()
	).toString();
	String ERROR_JSON_INVALID_APP_ID = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.invalidAppId.name()
	).toString();
	String ERROR_JSON_APP_UNDER_MAINTENANCE = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.appUnderMaintenance.name()
	).toString();
	String ERROR_JSON_INVALID_SESSION_ID = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.invalidSessionId.name()
	).toString();
	String ERROR_JSON_INVALID_USECASE = new GsonJsonObject().setString(JSON_KEY_STATUS,
		NetStatusCode.invalidUseCase.name()
	).toString();

	String STATUS_USECASEID_TEMPLATE = "{\"status\":\"%s\",\"uc\":\"%s\"}";
	String STATUS_SERVICE_USECASEID_MESSAGE_TEMPLATE = "{\"status\":\"%s\",\"service\":\"%s\",\"uc\":\"%s\",\"msg\":\"%s\"}";

	static String buildStatusWithUseCaseId(NetStatusCode code, String useCaseId)
	{
		return String.format(STATUS_USECASEID_TEMPLATE, code, useCaseId);
	}

	static void sendErrorToPotentialClient(
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		long requestId,
		UUID requestingUserId,
		String service,
		String useCaseId,
		Throwable throwable
	)
	{
		var message = String.format(STATUS_SERVICE_USECASEID_MESSAGE_TEMPLATE,
			NetStatusCode.internalError,
			service,
			useCaseId,
			throwable.getMessage()
		);
		if (requestingUserId == null)
		{
			responseSender.send(requestId, message);
		}
		else
		{
			userSender.send(requestingUserId, message);
		}
	}
}