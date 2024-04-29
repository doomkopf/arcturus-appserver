package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.system.app.App;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.message.ResponseOutgoingMessage;
import com.arcturus.appserver.system.message.UserOutgoingMessage;

import java.nio.ByteBuffer;

/**
 * Handling all the incoming messages from other nodes in the cluster forwarding
 * them to the respective service.
 *
 * @author doomkopf
 */
public class RemoteMessageHandler
{
	private final Logger log;
	private final AppManager appManager;
	private final ArcturusUserSender userSender;
	private final ArcturusResponseSender responseSender;
	private final RequestNodeContainer requestNodeContainer;

	public RemoteMessageHandler(
		LoggerFactory loggerFactory,
		AppManager appManager,
		ArcturusUserSender userSender,
		ArcturusResponseSender responseSender,
		RequestNodeContainer requestNodeContainer
	)
	{
		log = loggerFactory.create(getClass());
		this.appManager = appManager;
		this.userSender = userSender;
		this.responseSender = responseSender;
		this.requestNodeContainer = requestNodeContainer;
	}

	public void handleMessage(byte[] byteData)
	{
		forward(SerializableMessage.deserialize(ByteBuffer.wrap(byteData)));
	}

	private void forward(Message message)
	{
		var userOutgoingMessage = message.getUserOutgoingMessage();
		if (userOutgoingMessage != null)
		{
			userSender.send(userOutgoingMessage.getUserId(), userOutgoingMessage.getPayload());
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Received remote "
						+ UserOutgoingMessage.class.getSimpleName()
						+ " with payload: "
						+ userOutgoingMessage.getPayload()
				);
			}
			return;
		}

		var responseOutgoingMessage = message.getResponseOutgoingMessage();
		if (responseOutgoingMessage != null)
		{
			responseSender.send(responseOutgoingMessage.getRequestId(),
				responseOutgoingMessage.getPayload()
			);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Received remote "
						+ ResponseOutgoingMessage.class.getSimpleName()
						+ " with payload: "
						+ responseOutgoingMessage.getPayload()
				);
			}
			return;
		}

		var appManagementMessage = message.getAppManagementMessage();
		if (appManagementMessage != null)
		{
			try
			{
				appManagementMessage.getBehavior().execute(appManagementMessage, appManager);
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
			return;
		}

		var serviceMessage = message.getServiceMessage();
		if (serviceMessage == null)
		{
			log.log(LogLevel.info, "Unknown message");
			return;
		}

		var app = appManager.getAppIgnoringMaintenance(serviceMessage.getAppId());
		if (app == null)
		{
			log.log(LogLevel.info, App.MSG_APP_NOT_FOUND + serviceMessage.getAppId());
			return;
		}

		var service = app.getEntityServiceProvider().get(serviceMessage.getServiceName());
		if (service == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					UserEntityServiceProvider.MSG_SERVICE_NOT_FOUND
						+ serviceMessage.getServiceName()
						+ " appId: "
						+ serviceMessage.getAppId()
				);
			}
			return;
		}

		requestNodeContainer.put(findRequestId(serviceMessage.getMessage()),
			serviceMessage.getSourceNode()
		);

		service.getLocalService().send(serviceMessage.getMessage());
	}

	private static long findRequestId(Message message)
	{
		var domainMessage = message.getDomainMessage();
		if (domainMessage != null)
		{
			return domainMessage.getRequestId();
		}

		var domainTransactionMessage = message.getDomainTransactionMessage();
		if (domainTransactionMessage != null)
		{
			return domainTransactionMessage.getRequestId();
		}

		return 0;
	}
}
