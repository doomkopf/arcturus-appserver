package com.arcturus.appserver.system.maintainer.usecase.register;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.KeyValueStore.PutResult;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.maintainer.MaintenanceUseCaseHandler;
import com.arcturus.appserver.system.maintainer.entity.MaintainerAccount;
import com.arcturus.appserver.system.maintainer.entity.MaintainerUserToUserId;

import java.util.UUID;

public class MaintainerRegistration
	implements MaintenanceUseCaseHandler<MaintainerRegistrationRequest>
{
	private static final int MIN_USER_LENGTH = 3;
	private static final int MAX_USER_LENGTH = 32;
	private static final int MIN_PASSWORD_LENGTH = 3;
	private static final int MAX_PASSWORD_LENGTH = 32;

	private final Logger log;
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;

	public MaintainerRegistration(
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
	public Class<MaintainerRegistrationRequest> getRequestType()
	{
		return MaintainerRegistrationRequest.class;
	}

	@Override
	public void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		MaintainerRegistrationRequest request
	)
	{
		if ((request.getUser().length() < MIN_USER_LENGTH) || (request.getUser().length()
			> MAX_USER_LENGTH))
		{
			requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
				NetStatusCode.invalidUser)));
			return;
		}
		if ((request.getPassword().length() < MIN_PASSWORD_LENGTH) || (request.getPassword()
			.length() > MAX_PASSWORD_LENGTH))
		{
			requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
				NetStatusCode.invalidPassword)));
			return;
		}

		var newUserId = UUID.randomUUID();
		var user = request.getUser();
		var password = CryptTools.hashMD5(request.getPassword());

		createAccountEntity(newUserId, user, password, requestContext);
	}

	private void createAccountEntity(
		UUID userId, String user, String password, RequestContext requestContext
	)
	{
		db.asyncGet(DocumentKeys.maintainerUserToUserId(user), (key, value) ->
		{
			if (value != null)
			{
				requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
					NetStatusCode.invalidUser)));
				return;
			}

			db.asyncPut(
				DocumentKeys.maintainerUserIdToAccount(userId),
				jsonStringSerializer.toJsonString(new MaintainerAccount(password)),
				(key2, putResult) ->
				{
					if (putResult != PutResult.ok)
					{
						log.log(
							LogLevel.error,
							"Error creating "
								+ MaintainerAccount.class.getSimpleName()
								+ ": "
								+ putResult
						);
						requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
							NetStatusCode.internalError)));
						return;
					}

					createUserToUserIdEntity(user, userId, requestContext);
				}
			);
		});
	}

	private void createUserToUserIdEntity(String user, UUID userId, RequestContext requestContext)
	{
		String userToUserId;
		try
		{
			userToUserId = jsonStringSerializer.toJsonString(new MaintainerUserToUserId(userId));
		}
		catch (RuntimeException e)
		{
			log.log(LogLevel.error, e);
			requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
				NetStatusCode.jsonError)));
			return;
		}

		db.asyncPut(DocumentKeys.maintainerUserToUserId(user), userToUserId, (key, putResult) ->
		{
			if (putResult != PutResult.ok)
			{
				log.log(
					LogLevel.error,
					"Error creating "
						+ MaintainerUserToUserId.class.getSimpleName()
						+ ": "
						+ putResult
				);
				requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
					NetStatusCode.internalError)));
				return;
			}

			requestContext.respond(jsonStringSerializer.toJsonString(new MaintainerRegistrationResponse(
				NetStatusCode.ok)));
		});
	}
}