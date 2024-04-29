package com.arcturus.appserver.system.account.login;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.account.entity.LoginToken;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;
import com.arcturus.appserver.system.account.login.dto.LoginResponse;
import com.arcturus.appserver.system.account.login.dto.TokenLoginRequest;

public class TokenLoginHandler implements LoginHandler<TokenLoginRequest>
{
	private final Logger log;
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;

	public TokenLoginHandler(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer
	)
	{
		log = loggerFactory.create(getClass());
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public Class<TokenLoginRequest> requestType()
	{
		return TokenLoginRequest.class;
	}

	@Override
	public void handle(
		TokenLoginRequest request,
		String appId,
		String useCaseId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		LoginCallback loginCallback
	)
	{
		db.asyncGet(DocumentKeys.tokenToUserId(appId, request.getToken()),
			(key, jsonTokenToUserId) ->
			{
				if (jsonTokenToUserId == null)
				{
					LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
						requestContext,
						useCaseId,
						NetStatusCode.notFound
					);
					return;
				}

				LoginToken loginToken;
				try
				{
					loginToken = jsonStringSerializer.fromJsonString(LoginToken.class,
						jsonTokenToUserId
					);
				}
				catch (RuntimeException e)
				{
					log.log(LogLevel.error, e);
					LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
						requestContext,
						useCaseId,
						NetStatusCode.jsonError
					);
					return;
				}

				if (loginToken.isExpired())
				{
					LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
						requestContext,
						useCaseId,
						NetStatusCode.invalidToken
					);
					return;
				}

				loginCallback.login(loginToken.getUserId(),
					appId,
					requestContext,
					persistentLocalSession,
					useCaseId,
					null,
					loginToken.getLoginType()
				);
			}
		);
	}
}
