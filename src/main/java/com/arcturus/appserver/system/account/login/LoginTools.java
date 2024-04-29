package com.arcturus.appserver.system.account.login;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.KeyValueStore;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.account.AccountLoginType;
import com.arcturus.appserver.system.account.entity.LoginToken;
import com.arcturus.appserver.system.account.entity.UserIdToToken;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;

import java.util.UUID;

interface LoginTools
{
	static void storeNewTokenAndLogin(
		JsonStringSerializer jsonStringSerializer,
		KeyValueStore<String, String> db,
		String appId,
		UUID userId,
		AccountLoginType loginType,
		LoginCallback loginCallback,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String useCaseId
	)
	{
		var token = UUID.randomUUID().toString();
		var loginTokenDoc = jsonStringSerializer.toJsonString(new LoginToken(
			userId,
			loginType,
			System.currentTimeMillis() + Constants.LOGIN_TOKEN_VALIDITY_DURATION_MILLIS
		));

		db.asyncPut(DocumentKeys.tokenToUserId(appId, token), loginTokenDoc, (key, putResult) ->
		{
			var userIdToTokenDoc = jsonStringSerializer.toJsonString(new UserIdToToken(token));
			db.asyncPut(
				DocumentKeys.userIdToToken(appId, userId),
				userIdToTokenDoc,
				(key1, putResult1) -> loginCallback.login(
					userId,
					appId,
					requestContext,
					persistentLocalSession,
					useCaseId,
					token,
					loginType
				)
			);
		});
	}
}