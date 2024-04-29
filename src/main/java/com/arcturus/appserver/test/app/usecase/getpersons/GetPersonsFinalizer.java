package com.arcturus.appserver.test.app.usecase.getpersons;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.entity.list.CollectFinalMessage;
import com.arcturus.api.tool.JsonStringSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.UUID;

@UseCase(id = "getAllPersonsFinal")
public class GetPersonsFinalizer extends PojoPayloadUseCaseHandler<CollectFinalMessage<String>>
{
	private final ResponseSender responseSender;

	public GetPersonsFinalizer(
		JsonStringSerializer jsonStringSerializer, ResponseSender responseSender
	)
	{
		super(jsonStringSerializer);
		this.responseSender = responseSender;
	}

	@Override
	protected Type getPayloadType()
	{
		return new TypeToken<CollectFinalMessage<String>>()
		{
		}.getType();
	}

	@Override
	protected void handle(
		long requestId,
		UUID requestingUserId,
		CollectFinalMessage<String> payload,
		RequestInfo requestInfo
	)
	{
		responseSender.sendObject(requestId, new Response(payload.getElements()));
	}
}