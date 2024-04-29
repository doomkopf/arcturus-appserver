package com.arcturus.appserver.system.account.login;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.*;
import com.arcturus.appserver.system.account.AccountLoginType;
import com.arcturus.appserver.system.account.login.dto.LoginResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginManager
{
	@FunctionalInterface
	public interface LoginCallback
	{
		void login(
			UUID userId,
			String appId,
			RequestContext requestContext,
			PersistentLocalSession persistentLocalSession,
			String useCaseId,
			String token,
			AccountLoginType loginType
		);
	}

	private final Logger log;
	private final JsonStringSerializer jsonStringSerializer;
	private final UserSessionContainer userSessionContainer;
	private final UserNodeContainer userNodeContainer;
	private final SessionIdGenerator sessionIdGenerator;
	private final UUID localNodeId;
	private final Map<String, LoginHandler<?>> loginHandlers = new HashMap<>();

	public LoginManager(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer,
		UserSessionContainer userSessionContainer,
		UserNodeContainer userNodeContainer,
		SessionIdGenerator sessionIdGenerator,
		Cluster cluster,
		LoginHandler<?> tokenLoginHandler,
		LoginHandler<?> reconnectStatefulSessionHandler
	)
	{
		log = loggerFactory.create(getClass());
		this.jsonStringSerializer = jsonStringSerializer;
		this.userSessionContainer = userSessionContainer;
		this.userNodeContainer = userNodeContainer;
		this.sessionIdGenerator = sessionIdGenerator;

		loginHandlers.put(InternalUseCases.LOGIN,
			new UserAndPasswordLoginHandler(loggerFactory,
				DocumentKeys::userToUserId,
				db,
				jsonStringSerializer,
				user -> user
			)
		);
		loginHandlers.put(InternalUseCases.EMAIL_LOGIN, new UserAndPasswordLoginHandler(
			loggerFactory,
			DocumentKeys::emailToUserId,
			db,
			jsonStringSerializer,
			user -> user.toLowerCase(Constants.DEFAULT_LOCALE)
		));
		loginHandlers.put(InternalUseCases.TOKEN_LOGIN, tokenLoginHandler);
		loginHandlers.put(InternalUseCases.RECONNECT_STATEFUL_SESSION,
			reconnectStatefulSessionHandler
		);

		loginHandlers.put(InternalUseCases.FACEBOOK_LOGIN, new KeyLoginHandler(loggerFactory,
			db,
			DocumentKeys::facebookToUserId,
			jsonStringSerializer,
			AccountLoginType.facebook
		));

		loginHandlers.put(InternalUseCases.GOOGLE_LOGIN, new KeyLoginHandler(loggerFactory,
			db,
			DocumentKeys::googleToUserId,
			jsonStringSerializer,
			AccountLoginType.google
		));

		localNodeId = cluster.getLocalNode().getId();
	}

	public boolean handleLoginUseCases(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		String useCaseId,
		String jsonPayload
	)
	{
		var loginHandler = loginHandlers.get(useCaseId);
		if (loginHandler == null)
		{
			return false;
		}

		loginHandler.handle(jsonStringSerializer.fromJsonString(loginHandler.requestType(),
			jsonPayload
			),
			appId,
			useCaseId,
			requestContext,
			persistentLocalSession,
			this::login
		);

		return true;
	}

	private void login(
		UUID userId,
		String appId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String useCaseId,
		String token,
		AccountLoginType loginType
	)
	{
		var sessionId = sessionIdGenerator.generate();
		userSessionContainer.put(sessionId,
			userId,
			() -> userNodeContainer.put(userId, localNodeId, () ->
			{
				if (persistentLocalSession != null)
				{
					userSessionContainer.connectPersistenLocalSession(persistentLocalSession,
						userId,
						appId
					);
				}

				try
				{
					requestContext.respond(jsonStringSerializer.toJsonString(new LoginResponse(
						useCaseId,
						NetStatusCode.ok,
						Tools.encodeLongToRadix36String(sessionId),
						token,
						userId,
						loginType
					)));
				}
				catch (RuntimeException e)
				{
					log.log(LogLevel.error, e);
					LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
						requestContext,
						useCaseId,
						NetStatusCode.jsonError
					);
				}
			})
		);
	}
}
