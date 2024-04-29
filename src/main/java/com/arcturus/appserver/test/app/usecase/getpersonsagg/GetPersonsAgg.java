package com.arcturus.appserver.test.app.usecase.getpersonsagg;

import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.entity.aggregation.AggregationIndex;
import com.arcturus.api.service.entity.aggregation.AggregationService;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.api.tool.StringToUuidHasher;
import com.arcturus.appserver.test.app.TestService;

import java.util.Arrays;
import java.util.UUID;

@UseCase(id = "getPersonsAgg", isPublic = true)
public class GetPersonsAgg extends PojoPayloadUseCaseHandler<Request>
{
	private final AggregationService aggregationService;
	private final StringToUuidHasher stringToUuidHasher;

	public GetPersonsAgg(
		JsonStringSerializer jsonStringSerializer,
		AggregationService aggregationService,
		StringToUuidHasher stringToUuidHasher
	)
	{
		super(jsonStringSerializer);
		this.aggregationService = aggregationService;
		this.stringToUuidHasher = stringToUuidHasher;
	}

	@Override
	protected Class<Request> getPayloadType()
	{
		return Request.class;
	}

	@Override
	protected void handle(
		long requestId, UUID requestingUserId, Request payload, RequestInfo requestInfo
	)
	{
		aggregationService.start(
			TestService.person.name(),
			"map",
			"getAllPersonsAggFinal",
			Arrays.asList(new AggregationIndex(
				TestService.personNameList.name(),
				stringToUuidHasher.hash("testName" + payload.testSessionId)
			), new AggregationIndex(TestService.personAgeList.name(), new UUID(10, 0))),
			requestId,
			requestingUserId
		);
	}
}