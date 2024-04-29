package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.HttpSessionServiceType;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.system.message.UserOutgoingMessage;

import java.util.UUID;

public class ArcturusUserSender
{
	private final Logger log;
	private final UserSessionContainer userSessionContainer;
	private final UserNodeContainer userNodeContainer;
	private final Cluster cluster;
	private final boolean isHttpServerEnabled;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusUserSender(
		LoggerFactory loggerFactory,
		UserSessionContainer userSessionContainer,
		UserNodeContainer userNodeContainer,
		Cluster cluster,
		Config config,
		JsonStringSerializer jsonStringSerializer
	)
	{
		log = loggerFactory.create(getClass());
		this.userSessionContainer = userSessionContainer;
		this.userNodeContainer = userNodeContainer;
		this.cluster = cluster;
		this.jsonStringSerializer = jsonStringSerializer;

		isHttpServerEnabled = config.getEnum(HttpSessionServiceType.class,
			ServerConfigPropery.httpSessionServiceType
		)
			!= HttpSessionServiceType.empty;
	}

	public void send(UUID userId, String payload)
	{
		if (userId == null)
		{
			return;
		}

		if (!userSessionContainer.sendToPersistentLocalSessionsByUserId(userId, payload))
		{
			if (isHttpServerEnabled)
			{
				var httpRequestContext = userSessionContainer.getRequestContextByUserId(userId);
				if (httpRequestContext == null)
				{
					tryRemoteNode(userId, payload);
					return;
				}

				httpRequestContext.respond(payload);
			}
			else
			{
				tryRemoteNode(userId, payload);
			}
		}
	}

	private void tryRemoteNode(UUID userId, String payload)
	{
		userNodeContainer.getNodeIdByUserId(userId, nodeId ->
		{
			if (nodeId == null)
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, "No nodeId found for userId " + userId);
				}
				return;
			}

			var node = cluster.getNodeById(nodeId);
			if (node == null)
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, "No node found for userId " + userId);
				}
				return;
			}

			if (node.isLocal())
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(
						LogLevel.debug,
						"The user is neither connected locally nor remotely (outdated mapping) - skipping for userId "
							+ userId
					);
				}
				return;
			}

			var msg = new UserOutgoingMessage(userId, payload);
			msg.sendToNode(node);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Sent "
						+ UserOutgoingMessage.class.getSimpleName()
						+ " to remote node: "
						+ jsonStringSerializer.toJsonString(msg)
				);
			}
		});
	}
}
