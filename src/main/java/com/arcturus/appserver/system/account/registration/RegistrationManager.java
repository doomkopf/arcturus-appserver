package com.arcturus.appserver.system.account.registration;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.crypt.CryptTools;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.DocumentKeys.StringToDoc;
import com.arcturus.appserver.database.keyvaluestore.KeyValueStore.PutResult;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.json.gson.GsonJsonObject;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.account.dto.GenerateLoginDataResponse;
import com.arcturus.appserver.system.account.dto.SimpleRegistrationRequest;
import com.arcturus.appserver.system.account.entity.Account;
import com.arcturus.appserver.system.account.entity.UserToUserId;
import com.arcturus.appserver.system.account.password.PasswordGenerator;
import com.arcturus.appserver.system.account.password.PasswordValidator;

import java.util.UUID;

public class RegistrationManager
{
	private static final int MIN_USER_LENGTH = 3;
	private static final int MAX_USER_LENGTH = 32;

	private final Logger log;
	private final StringKeyValueStore db;
	private final JsonStringSerializer jsonStringSerializer;
	private final PasswordGenerator passwordGenerator;
	private final PasswordValidator passwordValidator;

	public RegistrationManager(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		JsonStringSerializer jsonStringSerializer,
		PasswordGenerator passwordGenerator,
		PasswordValidator passwordValidator
	)
	{
		log = loggerFactory.create(getClass());
		this.db = db;
		this.jsonStringSerializer = jsonStringSerializer;
		this.passwordGenerator = passwordGenerator;
		this.passwordValidator = passwordValidator;
	}

	public boolean handleRegistrationUseCases(
		RequestContext requestContext, String appId, String useCaseId, String jsonPayload
	)
	{
		StringToDoc func;
		String user;
		String email = null;
		String password;
		// TODO refactor by moving each case to a separate handler
		if (useCaseId.equals(InternalUseCases.GENERATE_LOGIN_DATA))
		{
			func = DocumentKeys::userToUserId;
			user = UUID.randomUUID().toString().replaceAll("-", "");
			password = passwordGenerator.generatePassword();
		}
		else if (useCaseId.equals(InternalUseCases.SIMPLE_REGISTRATION))
		{
			SimpleRegistrationRequest request = jsonStringSerializer.fromJsonString(SimpleRegistrationRequest.class,
				jsonPayload
			);

			user = request.getUser().trim();
			password = request.getPassword().trim();

			if ((user.length() < MIN_USER_LENGTH) || (user.length() > MAX_USER_LENGTH))
			{
				sendRegResponse(requestContext, useCaseId, NetStatusCode.invalidUser);
				return true;
			}
			if (!passwordValidator.validatePassword(password))
			{
				sendRegResponse(requestContext, useCaseId, NetStatusCode.invalidPassword);
				return true;
			}

			func = DocumentKeys::userToUserId;
		}
		else if (useCaseId.equals(InternalUseCases.EMAIL_REGISTRATION))
		{
			SimpleRegistrationRequest request = jsonStringSerializer.fromJsonString(SimpleRegistrationRequest.class,
				jsonPayload
			);

			user = request.getUser().trim().toLowerCase(Constants.DEFAULT_LOCALE);
			password = request.getPassword().trim();

			if (user.isEmpty()
				|| (user.length() > Constants.MAX_EMAIL_LENGTH)
				|| !Constants.EMAIL_REGEX.matcher(user).matches())
			{
				sendRegResponse(requestContext, useCaseId, NetStatusCode.invalidEmail);
				return true;
			}

			email = user;

			func = DocumentKeys::emailToUserId;
		}
		else
		{
			return false;
		}

		password = CryptTools.hashMD5(password);

		createAccountEntity(func,
			appId,
			UUID.randomUUID(),
			user,
			email,
			password,
			requestContext,
			useCaseId
		);

		return true;
	}

	private void createAccountEntity(
		StringToDoc func,
		String appId,
		UUID userId,
		String user,
		String email,
		String password,
		RequestContext requestContext,
		String useCaseId
	)
	{
		db.asyncGet(func.key(appId, user), (key, value) ->
		{
			if (value != null)
			{
				sendRegResponse(requestContext, useCaseId, NetStatusCode.invalidUser);
				return;
			}

			db.asyncPut(DocumentKeys.userIdToAccount(appId, userId),
				jsonStringSerializer.toJsonString(new Account(email, password)),
				(key2, putResult) ->
				{
					if (putResult != PutResult.ok)
					{
						log.log(LogLevel.error,
							"Error creating " + Account.class.getSimpleName() + ": " + putResult
						);
						sendRegResponse(requestContext, useCaseId, NetStatusCode.internalError);
						return;
					}

					createStringToUserIdEntity(func,
						appId,
						user,
						password,
						userId,
						requestContext,
						useCaseId
					);
				}
			);
		});
	}

	private void createStringToUserIdEntity(
		StringToDoc func,
		String appId,
		String user,
		String password,
		UUID userId,
		RequestContext requestContext,
		String useCaseId
	)
	{
		String userToUserId;
		try
		{
			userToUserId = jsonStringSerializer.toJsonString(new UserToUserId(userId));
		}
		catch (RuntimeException e)
		{
			log.log(LogLevel.error, e);
			sendRegResponse(requestContext, useCaseId, NetStatusCode.jsonError);
			return;
		}

		db.asyncPut(func.key(appId, user), userToUserId, (key, putResult) ->
		{
			if (putResult != PutResult.ok)
			{
				log.log(LogLevel.error,
					"Error creating " + UserToUserId.class.getSimpleName() + ": " + putResult
				);
				sendRegResponse(requestContext, useCaseId, NetStatusCode.internalError);
				return;
			}

			// TODO refactor as well (see above)
			if (useCaseId.equals(InternalUseCases.GENERATE_LOGIN_DATA))
			{
				String response;
				try
				{
					response = jsonStringSerializer.toJsonString(new GenerateLoginDataResponse(user,
						password
					));
				}
				catch (RuntimeException e)
				{
					log.log(LogLevel.error, e);
					sendRegResponse(requestContext, useCaseId, NetStatusCode.jsonError);
					return;
				}

				requestContext.respond(response);
			}
			else if (useCaseId.equals(InternalUseCases.SIMPLE_REGISTRATION) || useCaseId.equals(
				InternalUseCases.EMAIL_REGISTRATION))
			{
				sendRegResponse(requestContext, useCaseId, NetStatusCode.ok);
			}
		});
	}

	private static void sendRegResponse(
		RequestContext requestContext, String useCaseId, NetStatusCode status
	)
	{
		requestContext.respond(new GsonJsonObject().setString(Constants.JSONKEY_USECASE, useCaseId)
			.setString(NetCodes.JSON_KEY_STATUS, status.name())
			.toString());
	}
}
