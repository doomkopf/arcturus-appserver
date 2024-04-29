package com.arcturus.appserver.test.app.usecase.createperson;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.service.entity.list.ListService;
import com.arcturus.api.service.entity.list.ListServiceProvider;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.api.tool.StringToUuidHasher;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.test.app.TestService;
import com.arcturus.appserver.test.app.service.person.Person;

import java.util.UUID;

@EntityUseCase(id = "createPerson", service = "person", isCreateEntity = true, isPublic = true)
public class CreatePerson extends PojoPayloadEntityUseCaseHandler<Person, Request>
{
	private final ListServiceProvider listServiceProvider;
	private final ResponseSender responseSender;
	private final StringToUuidHasher stringToUuidHasher;

	public CreatePerson(
		JsonStringSerializer jsonStringSerializer,
		ListServiceProvider listServiceProvider,
		ResponseSender responseSender,
		StringToUuidHasher stringToUuidHasher
	)
	{
		super(jsonStringSerializer);
		this.listServiceProvider = listServiceProvider;
		this.responseSender = responseSender;
		this.stringToUuidHasher = stringToUuidHasher;
	}

	@Override
	protected Class<Request> getPayloadType()
	{
		return Request.class;
	}

	@Override
	protected void handle(
		Person entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Request payload,
		UseCaseContext context
	)
	{
		entity.setName(payload.name);
		entity.setAge(payload.age);
		context.dirty();

		ListService<String> nameTestListService = listServiceProvider.getServiceByName(TestService.personNameTestList
			.name());
		nameTestListService.add(entity.getName(), payload.testSessionId);

		ListService<UUID> personNameListService = listServiceProvider.getServiceByName(TestService.personNameList
			.name());
		personNameListService.add(id, stringToUuidHasher.hash(entity.getName()));
		ListService<UUID> personAgeListService = listServiceProvider.getServiceByName(TestService.personAgeList
			.name());
		personAgeListService.add(id, new UUID(entity.getAge(), 0));

		responseSender.sendObject(requestId, new Response(NetStatusCode.ok.name()));
	}
}