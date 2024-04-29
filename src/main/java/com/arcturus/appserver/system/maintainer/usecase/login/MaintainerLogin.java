package com.arcturus.appserver.system.maintainer.usecase.login;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.SessionIdGenerator;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.UserSessionContainer;
import com.arcturus.appserver.system.maintainer.MaintenanceUseCaseHandler;
import com.arcturus.appserver.system.maintainer.entity.MaintainerAccount;
import com.arcturus.appserver.system.maintainer.entity.MaintainerUserToUserId;

import java.util.UUID;

public class MaintainerLogin implements MaintenanceUseCaseHandler<MaintainerLoginRequest>
{
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;
	private final SessionIdGenerator sessionIdGenerator;
	private final UserSessionContainer userSessionContainer;

	public MaintainerLogin(
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer,
		SessionIdGenerator sessionIdGenerator,
		UserSessionContainer userSessionContainer
	)
	{
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
		this.sessionIdGenerator = sessionIdGenerator;
		this.userSessionContainer = userSessionContainer;
	}

	@Override
	public Class<MaintainerLoginRequest> getRequestType()
	{
		return MaintainerLoginRequest.class;
	}

	@Override
	public void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		MaintainerLoginRequest request
	)
	{
		db.asyncGet(DocumentKeys.maintainerUserToUserId(request.getUser()),
			(key, jsonUserToUserId) ->
			{
				if (jsonUserToUserId == null)
				{
					requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerLoginResponse(
						NetStatusCode.notFound)));
					return;
				}

				MaintainerUserToUserId userToUserIdEntity = jsonStringSerializer.fromJsonString(MaintainerUserToUserId.class,
					jsonUserToUserId
				);

				authenticateAndLogin(userToUserIdEntity.getUserId(),
					request.getPassword(),
					appId,
					requestContext,
					persistentLocalSession
				);
			}
		);
	}

	private void authenticateAndLogin(
		UUID userId,
		String givenPassword,
		String appId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession
	)
	{
		db.asyncGet(DocumentKeys.maintainerUserIdToAccount(userId), (key2, jsonAccount) ->
		{
			if (jsonAccount == null)
			{
				requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerLoginResponse(
					NetStatusCode.notFound)));
				return;
			}

			MaintainerAccount account = jsonStringSerializer.fromJsonString(MaintainerAccount.class,
				jsonAccount
			);

			if (!account.getPassword().equals(CryptTools.hashMD5(givenPassword)))
			{
				requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerLoginResponse(
					NetStatusCode.invalidPassword)));
				return;
			}

			login(Long.valueOf(sessionIdGenerator.generate()),
				userId,
				appId,
				requestContext,
				persistentLocalSession
			);
		});
	}

	private void login(
		Long sessionId,
		UUID userId,
		String appId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession
	)
	{
		if (persistentLocalSession != null)
		{
			userSessionContainer.connectPersistenLocalSession(persistentLocalSession,
				userId,
				appId
			);
		}

		userSessionContainer.put(sessionId,
			userId,
			() -> requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerLoginResponse(
				NetStatusCode.ok,
				Tools.encodeLongToRadix36String(sessionId.longValue())
			)))
		);
	}
}
