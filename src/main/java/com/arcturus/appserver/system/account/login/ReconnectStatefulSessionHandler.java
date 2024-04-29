package com.arcturus.appserver.system.account.login;

import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.UserSessionContainer;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;
import com.arcturus.appserver.system.account.login.dto.ReconnectStatefulSessionRequest;

public class ReconnectStatefulSessionHandler
	implements LoginHandler<ReconnectStatefulSessionRequest>
{
	private final UserSessionContainer userSessionContainer;

	public ReconnectStatefulSessionHandler(
		UserSessionContainer userSessionContainer
	)
	{
		this.userSessionContainer = userSessionContainer;
	}

	@Override
	public Class<ReconnectStatefulSessionRequest> requestType()
	{
		return ReconnectStatefulSessionRequest.class;
	}

	@Override
	public void handle(
		ReconnectStatefulSessionRequest request,
		String appId,
		String useCaseId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		LoginCallback loginCallback
	)
	{
		if (persistentLocalSession == null)
		{
			requestContext.respond(NetCodes.buildStatusWithUseCaseId(NetStatusCode.invalidRequest,
				InternalUseCases.RECONNECT_STATEFUL_SESSION
			));
			return;
		}

		var sessionId = Tools.parseLongFromRadix36EncodedString(request.getSessionId());
		userSessionContainer.getUserIdBySessionId(sessionId, userId ->
		{
			if (userId == null)
			{
				requestContext.respond(NetCodes.buildStatusWithUseCaseId(NetStatusCode.invalidSessionId,
					InternalUseCases.RECONNECT_STATEFUL_SESSION
				));
				return;
			}

			userSessionContainer.connectPersistenLocalSession(persistentLocalSession,
				userId,
				appId
			);

			requestContext.respond(NetCodes.buildStatusWithUseCaseId(NetStatusCode.ok,
				InternalUseCases.RECONNECT_STATEFUL_SESSION
			));
		});
	}
}