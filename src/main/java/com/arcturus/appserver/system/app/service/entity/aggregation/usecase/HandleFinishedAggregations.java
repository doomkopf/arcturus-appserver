package com.arcturus.appserver.system.app.service.entity.aggregation.usecase;

import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationContext;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.HandleFinishedAggregations.Message;

import java.lang.reflect.Type;
import java.util.UUID;

public class HandleFinishedAggregations
	extends PojoPayloadEntityUseCaseHandler<AggregationContext, Message>
{
	public static class Message
	{
		String agg;

		private Message()
		{
		}

		public Message(String serializedEntityAggreate)
		{
			agg = serializedEntityAggreate;
		}
	}

	private final UseCaseProcessor useCaseProcessor;

	public HandleFinishedAggregations(
		UseCaseProcessor useCaseProcessor, JsonStringSerializer jsonStringSerializer
	)
	{
		super(jsonStringSerializer);
		this.useCaseProcessor = useCaseProcessor;
	}

	@Override
	protected Type getPayloadType()
	{
		return Message.class;
	}

	@Override
	protected void handle(
		AggregationContext aggregationContext,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Message message,
		UseCaseContext useCaseContext
	)
	{
		aggregationContext.finishedEntityAggregation(message.agg,
			useCaseProcessor,
			requestId,
			requestingUserId
		);
	}
}