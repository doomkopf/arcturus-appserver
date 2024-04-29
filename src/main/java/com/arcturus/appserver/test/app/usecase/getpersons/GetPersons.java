package com.arcturus.appserver.test.app.usecase.getpersons;

import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.api.service.entity.list.ListServiceProvider;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.TestService;

import java.util.UUID;

@UseCase(id = "getAllPersons", isPublic = true)
public class GetPersons extends PojoPayloadUseCaseHandler<Request>
{
	private final ListServiceProvider listServiceProvider;

	public GetPersons(
		JsonStringSerializer jsonStringSerializer, ListServiceProvider listServiceProvider
	)
	{
		super(jsonStringSerializer);
		this.listServiceProvider = listServiceProvider;
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
		ListService<String> listService = listServiceProvider.getServiceByName(TestService.personNameTestList
			.name());
		listService.collect("getAllPersonsFinal",
			payload.testSessionId,
			requestId,
			requestingUserId
		);
	}
}