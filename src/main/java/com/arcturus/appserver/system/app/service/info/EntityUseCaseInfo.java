package com.arcturus.appserver.system.app.service.info;

public class EntityUseCaseInfo implements UseCaseInfo
{
	private final String id;
	private final boolean isCreateEntity;
	private final boolean isPublic;
	private final String description;
	private final String requestBody;
	private final String successResponseBody;

	public EntityUseCaseInfo(
		String id,
		boolean isCreateEntity,
		boolean isPublic,
		String description,
		String requestBody,
		String successResponseBody
	)
	{
		this.id = id;
		this.isCreateEntity = isCreateEntity;
		this.isPublic = isPublic;
		this.description = (description == null) ? "" : description;
		this.requestBody = (requestBody == null) ? "{}" : requestBody;
		this.successResponseBody = (successResponseBody == null) ? "{}" : successResponseBody;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public boolean isCreateEntity()
	{
		return isCreateEntity;
	}

	@Override
	public boolean isPublic()
	{
		return isPublic;
	}

	@Override
	public String getRequestBody()
	{
		return requestBody;
	}

	@Override
	public String getSuccessResponseBody()
	{
		return successResponseBody;
	}
}