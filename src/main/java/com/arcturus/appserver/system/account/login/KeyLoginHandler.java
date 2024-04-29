package com.arcturus.appserver.system.account.login;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.DocumentKeys.StringToDoc;
import com.arcturus.appserver.database.keyvaluestore.KeyValueStore.PutResult;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.account.AccountLoginType;
import com.arcturus.appserver.system.account.entity.Account;
import com.arcturus.appserver.system.account.entity.UserToUserId;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;
import com.arcturus.appserver.system.account.login.dto.KeyLoginRequest;

import java.util.UUID;

public class KeyLoginHandler implements LoginHandler<KeyLoginRequest>
{
	private final Logger log;
	private final StringKeyValueStore db;
	private final StringToDoc func;
	private final JsonStringSerializer jsonStringSerializer;
	private final AccountLoginType accountLoginType;

	KeyLoginHandler(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		StringToDoc func,
		JsonStringSerializer jsonStringSerializer,
		AccountLoginType accountLoginType
	)
	{
		log = loggerFactory.create(getClass());
		this.db = db;
		this.func = func;
		this.jsonStringSerializer = jsonStringSerializer;
		this.accountLoginType = accountLoginType;
	}

	@Override
	public Class<KeyLoginRequest> requestType()
	{
		return KeyLoginRequest.class;
	}

	@Override
	public void handle(
		KeyLoginRequest request,
		String appId,
		String useCaseId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		LoginCallback loginCallback
	)
	{
		var docKey = func.key(appId, request.key);
		db.asyncGet(docKey, (key, value) ->
		{
			if (value == null)
			{
				var userId = UUID.randomUUID();
				var userToUserId = new UserToUserId(userId);
				db.asyncPut(docKey,
					jsonStringSerializer.toJsonString(userToUserId),
					(key1, putResult) -> db.asyncPut(DocumentKeys.userIdToAccount(appId, userId),
						jsonStringSerializer.toJsonString(new Account(null, null)),
						(key2, putResult2) ->
						{
							if (putResult != PutResult.ok)
							{
								log.log(LogLevel.error,
									"Error creating "
										+ Account.class.getSimpleName()
										+ ": "
										+ putResult
								);
							}

							LoginTools.storeNewTokenAndLogin(jsonStringSerializer,
								db,
								appId,
								userId,
								accountLoginType,
								loginCallback,
								requestContext,
								persistentLocalSession,
								useCaseId
							);
						}
					)
				);
				return;
			}

			UserToUserId userToUserId = jsonStringSerializer.fromJsonString(UserToUserId.class,
				value
			);

			LoginTools.storeNewTokenAndLogin(jsonStringSerializer,
				db,
				appId,
				userToUserId.getUserId(),
				accountLoginType,
				loginCallback,
				requestContext,
				persistentLocalSession,
				useCaseId
			);
		});
	}
}