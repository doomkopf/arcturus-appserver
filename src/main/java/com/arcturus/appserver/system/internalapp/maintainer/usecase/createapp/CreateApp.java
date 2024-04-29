package com.arcturus.appserver.system.internalapp.maintainer.usecase.createapp;

import com.arcturus.api.UserSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.internalapp.maintainer.dto.MaintainerAppDto;
import com.arcturus.appserver.system.internalapp.maintainer.service.maintainer.Maintainer;

import java.util.UUID;

@EntityUseCase(id = InternalUseCases.CREATE_APP, service = ArcturusEntityService.SERVICE_NAME_USER, isCreateEntity = true, isPublic = true)
public class CreateApp extends PojoPayloadEntityUseCaseHandler<Maintainer, CreateAppRequest>
{
	private static final int MIN_APP_NAME_LENGTH = 2;
	private static final int MAX_APP_NAME_LENGTH = 32;

	private final UserSender userSender;

	public CreateApp(JsonStringSerializer jsonStringSerializer, UserSender userSender)
	{
		super(jsonStringSerializer);

		this.userSender = userSender;
	}

	@Override
	protected Class<CreateAppRequest> getPayloadType()
	{
		return CreateAppRequest.class;
	}

	@Override
	protected void handle(
		Maintainer entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		CreateAppRequest payload,
		UseCaseContext context
	)
	{
		if (requestingUserId == null)
		{
			return;
		}

		if ((payload.name.length() < MIN_APP_NAME_LENGTH) || (payload.name.length()
			> MAX_APP_NAME_LENGTH))
		{
			return;
		}

		var app = entity.createApp(payload.name);

		context.dirty();

		userSender.sendObject(requestingUserId,
			new CreateAppResponse(NetStatusCode.ok,
				new MaintainerAppDto(app.getId(), app.getName())
			)
		);
	}
}
