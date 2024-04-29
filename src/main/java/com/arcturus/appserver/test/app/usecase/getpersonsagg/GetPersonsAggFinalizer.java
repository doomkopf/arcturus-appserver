package com.arcturus.appserver.test.app.usecase.getpersonsagg;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.UserSender;
import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.entity.list.CollectFinalMessage;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.usecase.usersenderpush.UserSenderPushResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.stream.Collectors;

@UseCase(id = "getAllPersonsAggFinal")
public class GetPersonsAggFinalizer
	extends PojoPayloadUseCaseHandler<CollectFinalMessage<PersonAggregate>>
{
	private final ResponseSender responseSender;
	private final UserSender userSender;

	public GetPersonsAggFinalizer(
		JsonStringSerializer jsonStringSerializer,
		ResponseSender responseSender,
		UserSender userSender
	)
	{
		super(jsonStringSerializer);
		this.responseSender = responseSender;
		this.userSender = userSender;
	}

	@Override
	protected Type getPayloadType()
	{
		return new TypeToken<CollectFinalMessage<PersonAggregate>>()
		{
		}.getType();
	}

	@Override
	protected void handle(
		long requestId,
		UUID requestingUserId,
		CollectFinalMessage<PersonAggregate> payload,
		RequestInfo requestInfo
	)
	{
		responseSender.sendObject(
			requestId,
			new Response(payload.getElements()
				.stream()
				.map(PersonAggregate::getName)
				.collect(Collectors.toList()))
		);

		userSender.sendObject(requestingUserId, new UserSenderPushResponse());
	}
}
