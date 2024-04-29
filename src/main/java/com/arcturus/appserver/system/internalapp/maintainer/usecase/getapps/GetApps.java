package com.arcturus.appserver.system.internalapp.maintainer.usecase.getapps;

import com.arcturus.api.UserSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.internalapp.maintainer.dto.MaintainerAppDto;
import com.arcturus.appserver.system.internalapp.maintainer.service.maintainer.Maintainer;

import java.util.UUID;
import java.util.stream.Collectors;

@EntityUseCase(id = InternalUseCases.GET_APPS, service = ArcturusEntityService.SERVICE_NAME_USER, isCreateEntity = true, isPublic = true)
public class GetApps extends PojoPayloadEntityUseCaseHandler<Maintainer, GetAppsRequest>
{
	private final UserSender userSender;

	public GetApps(JsonStringSerializer jsonStringSerializer, UserSender userSender)
	{
		super(jsonStringSerializer);

		this.userSender = userSender;
	}

	@Override
	protected Class<GetAppsRequest> getPayloadType()
	{
		return GetAppsRequest.class;
	}

	@Override
	protected void handle(
		Maintainer entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		GetAppsRequest payload,
		UseCaseContext context
	)
	{
		userSender.sendObject(
			requestingUserId,
			new GetAppsResponse(entity.getApps()
				.stream()
				.map(app -> new MaintainerAppDto(app.getId(), app.getName()))
				.collect(Collectors.toList()))
		);
	}
}
