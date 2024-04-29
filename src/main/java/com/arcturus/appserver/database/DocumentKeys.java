package com.arcturus.appserver.database;

import java.util.UUID;

public interface DocumentKeys // NOSONAR
{
	String MAINTAINER_PREFIX = "m";

	@FunctionalInterface
	interface StringToDoc
	{
		String key(String appId, String str);
	}

	static String userToUserId(String appId, String user)
	{
		return appId + "_user2id_" + user;
	}

	static String emailToUserId(String appId, String email)
	{
		return appId + "_email2id_" + email;
	}

	static String facebookToUserId(String appId, String facebook)
	{
		return appId + "_facebook2id_" + facebook;
	}

	static String googleToUserId(String appId, String google)
	{
		return appId + "_google2id_" + google;
	}

	static String tokenToUserId(String appId, String token)
	{
		return appId + "_token2id_" + token;
	}

	static String userIdToToken(String appId, UUID userId)
	{
		return appId + "_id2token_" + userId;
	}

	static String userIdToAccount(String appId, UUID userId)
	{
		return appId + "_id2account_" + userId;
	}

	static String maintainerUserToUserId(String user)
	{
		return MAINTAINER_PREFIX + "_user2id_" + user;
	}

	static String maintainerUserIdToAccount(UUID userId)
	{
		return MAINTAINER_PREFIX + "_id2account_" + userId;
	}

	static String appScript(String appId)
	{
		return appId + "_script";
	}

	static String entity(String appId, String serviceName, String id)
	{
		return appId + '_' + serviceName + '_' + id;
	}

	static String passwordResetKeyToUserId(String appId, String passwordResetKey)
	{
		return appId + "_pwreset_" + passwordResetKey;
	}
}
