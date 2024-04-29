package com.arcturus.appserver.system.account;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.json.gson.GsonJsonObject;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.account.dto.IsEmailRegisteredRequest;
import com.arcturus.appserver.system.account.dto.IsEmailRegisteredResponse;
import com.arcturus.appserver.system.account.entity.Account;
import com.arcturus.appserver.system.account.login.LoginManager;
import com.arcturus.appserver.system.account.login.dto.GetEmailResponse;
import com.arcturus.appserver.system.account.password.PasswordResetManager;
import com.arcturus.appserver.system.account.registration.RegistrationManager;

import java.util.UUID;

/**
 * Handling all account (e.g. reg or login) related use cases.
 *
 * @author doomkopf
 */
public class AccountManager
{
	private final LoginManager loginManager;
	private final RegistrationManager registrationManager;
	private final PasswordResetManager passwordResetManager;

	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;

	public AccountManager(
		LoginManager loginManager,
		RegistrationManager registrationManager,
		PasswordResetManager passwordResetManager,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer
	)
	{
		this.loginManager = loginManager;
		this.registrationManager = registrationManager;
		this.passwordResetManager = passwordResetManager;
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public boolean handleAccountUseCases(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		String useCaseId,
		UUID userId,
		String jsonPayload
	)
	{
		if (loginManager.handleLoginUseCases(requestContext,
			persistentLocalSession,
			appId,
			useCaseId,
			jsonPayload
		))
		{
			return true;
		}

		if (registrationManager.handleRegistrationUseCases(requestContext,
			appId,
			useCaseId,
			jsonPayload
		))
		{
			return true;
		}

		if (passwordResetManager.handlePasswordResetUseCases(requestContext,
			appId,
			useCaseId,
			userId,
			jsonPayload
		))
		{
			return true;
		}

		if (useCaseId.equals(InternalUseCases.IS_EMAIL_REGISTERED))
		{
			handleIsEmailRegistered(requestContext, appId, jsonPayload);
			return true;
		}

		if (useCaseId.equals(InternalUseCases.GET_EMAIL))
		{
			handleGetEmail(requestContext, appId, userId);
			return true;
		}

		return false;
	}

	private void handleIsEmailRegistered(
		RequestContext requestContext, String appId, String jsonPayload
	)
	{
		IsEmailRegisteredRequest request = jsonStringSerializer.fromJsonString(IsEmailRegisteredRequest.class,
			jsonPayload
		);

		var email = request.email.toLowerCase(Constants.DEFAULT_LOCALE);

		db.asyncGet(DocumentKeys.emailToUserId(appId, email),
			(key, value) -> requestContext.respond(jsonStringSerializer.toJsonString(new IsEmailRegisteredResponse(
				value != null)))
		);
	}

	private void handleGetEmail(RequestContext requestContext, String appId, UUID userId)
	{
		if (userId == null)
		{
			sendResponse(requestContext, InternalUseCases.GET_EMAIL, NetStatusCode.invalidUser);
			return;
		}

		db.asyncGet(DocumentKeys.userIdToAccount(appId, userId), (key, value) ->
		{
			if (value == null)
			{
				sendResponse(requestContext, InternalUseCases.GET_EMAIL, NetStatusCode.notFound);
				return;
			}

			Account account = jsonStringSerializer.fromJsonString(Account.class, value);
			requestContext.respond(jsonStringSerializer.toJsonString(new GetEmailResponse(InternalUseCases.GET_EMAIL,
				NetStatusCode.ok,
				account.getEmail()
			)));
		});
	}

	private static void sendResponse(
		RequestContext requestContext, String useCaseId, NetStatusCode statusCode
	)
	{
		requestContext.respond(new GsonJsonObject().setString(Constants.JSONKEY_USECASE, useCaseId)
			.setString(NetCodes.JSON_KEY_STATUS, statusCode.name())
			.toString());
	}
}
