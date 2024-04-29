package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.system.message.ResponseOutgoingMessage;

public class ArcturusResponseSender
{
	private final Logger log;
	private final RequestsContainer requestsContainer;
	private final RequestNodeContainer requestNodeContainer;
	private final Cluster cluster;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusResponseSender(
		LoggerFactory loggerFactory,
		RequestsContainer requestsContainer,
		RequestNodeContainer requestNodeContainer,
		Cluster cluster,
		JsonStringSerializer jsonStringSerializer
	)
	{
		log = loggerFactory.create(getClass());
		this.requestsContainer = requestsContainer;
		this.requestNodeContainer = requestNodeContainer;
		this.cluster = cluster;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public void send(long requestId, String payload)
	{
		var requestContext = requestsContainer.remove(requestId);
		if (requestContext == null)
		{
			tryRemoteNode(requestId, payload);
			return;
		}

		requestContext.respond(payload);
	}

	private void tryRemoteNode(long requestId, String payload)
	{
		var nodeId = requestNodeContainer.getNodeIdByRequestId(requestId);
		if (nodeId == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "No nodeId found for requestId " + requestId);
			}
			return;
		}

		var node = cluster.getNodeById(nodeId);
		if (node == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "No node found for requestId " + requestId);
			}
			return;
		}

		if (node.isLocal())
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(
					LogLevel.debug,
					"The client is neither connected locally nor remotely (outdated mapping) - skipping for requestId "
						+ requestId
				);
			}
			return;
		}

		var msg = new ResponseOutgoingMessage(requestId, payload);
		msg.sendToNode(node);
		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(
				LogLevel.debug,
				"Sent "
					+ ResponseOutgoingMessage.class.getSimpleName()
					+ " to remote node: "
					+ jsonStringSerializer.toJsonString(msg)
			);
		}
	}
}