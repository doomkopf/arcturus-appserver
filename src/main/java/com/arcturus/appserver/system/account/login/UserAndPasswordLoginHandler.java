package com.arcturus.appserver.system.account.login;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.DocumentKeys.StringToDoc;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.account.AccountLoginType;
import com.arcturus.appserver.system.account.entity.Account;
import com.arcturus.appserver.system.account.entity.UserIdToToken;
import com.arcturus.appserver.system.account.entity.UserToUserId;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;
import com.arcturus.appserver.system.account.login.dto.LoginRequest;
import com.arcturus.appserver.system.account.login.dto.LoginResponse;

import java.util.UUID;

public class UserAndPasswordLoginHandler implements LoginHandler<LoginRequest>
{
	public interface UserProcessor
	{
		String processUser(String user);
	}

	private final Logger log;
	private final StringToDoc stringToDoc;
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;
	private final UserProcessor userProcessor;

	UserAndPasswordLoginHandler(
		LoggerFactory loggerFactory,
		StringToDoc stringToDoc,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer,
		UserProcessor userProcessor
	)
	{
		log = loggerFactory.create(getClass());
		this.stringToDoc = stringToDoc;
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
		this.userProcessor = userProcessor;
	}

	@Override
	public Class<LoginRequest> requestType()
	{
		return LoginRequest.class;
	}

	@Override
	public void handle(
		LoginRequest request,
		String appId,
		String useCaseId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		LoginCallback loginCallback
	)
	{
		var user = request.getUser().trim();
		var password = request.getPassword().trim();

		db.asyncGet(stringToDoc.key(appId, userProcessor.processUser(user)),
			(key, jsonUserToUserId) ->
			{
				if (jsonUserToUserId == null)
				{
					LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
						requestContext,
						useCaseId,
						NetStatusCode.notFound
					);
					return;
				}

				UserToUserId userToUserIdEntity;
				try
				{
					userToUserIdEntity = jsonStringSerializer.fromJsonString(UserToUserId.class,
						jsonUserToUserId
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

				passwordCheckAndLogin(appId,
					userToUserIdEntity.getUserId(),
					password,
					requestContext,
					persistentLocalSession,
					useCaseId,
					loginCallback
				);
			}
		);
	}

	private void passwordCheckAndLogin(
		String appId,
		UUID userId,
		String givenPassword,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String useCaseId,
		LoginCallback loginCallback
	)
	{
		db.asyncGet(DocumentKeys.userIdToAccount(appId, userId), (key2, jsonAccount) ->
		{
			if (jsonAccount == null)
			{
				LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
					requestContext,
					useCaseId,
					NetStatusCode.notFound
				);
				return;
			}

			Account account;
			try
			{
				account = jsonStringSerializer.fromJsonString(Account.class, jsonAccount);
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

			if (!account.getPassword().equals(CryptTools.hashMD5(givenPassword)))
			{
				LoginResponse.sendLoginErrorResponse(jsonStringSerializer,
					requestContext,
					useCaseId,
					NetStatusCode.invalidPassword
				);
				return;
			}

			getAndRemovePotentialTokenAndLogin(appId,
				userId,
				loginCallback,
				requestContext,
				persistentLocalSession,
				useCaseId
			);
		});
	}

	private void getAndRemovePotentialTokenAndLogin(
		String appId,
		UUID userId,
		LoginCallback loginCallback,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String useCaseId
	)
	{
		db.asyncGet(DocumentKeys.userIdToToken(appId, userId), (key, jsonToken) ->
		{
			if (jsonToken == null)
			{
				LoginTools.storeNewTokenAndLogin(jsonStringSerializer,
					db,
					appId,
					userId,
					AccountLoginType.std,
					loginCallback,
					requestContext,
					persistentLocalSession,
					useCaseId
				);
			}
			else
			{
				UserIdToToken userIdToToken = jsonStringSerializer.fromJsonString(UserIdToToken.class,
					jsonToken
				);
				db.asyncRemove(DocumentKeys.tokenToUserId(appId, userIdToToken.getToken()),
					(k, removeResult) -> LoginTools.storeNewTokenAndLogin(jsonStringSerializer,
						db,
						appId,
						userId,
						AccountLoginType.std,
						loginCallback,
						requestContext,
						persistentLocalSession,
						useCaseId
					)
				);
			}
		});
	}
}
