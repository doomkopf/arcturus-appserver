package com.arcturus.appserver.system.app.service.info;

public class ServicelessUseCaseInfo implements UseCaseInfo
{
	private final String id;
	private final boolean isPublic;
	private final String description;
	private final String requestBody;
	private final String successResponseBody;

	public ServicelessUseCaseInfo(
		String id,
		boolean isPublic,
		String description,
		String requestBody,
		String successResponseBody
	)
	{
		this.id = id;
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

	@Override
	public boolean isPublic()
	{
		return isPublic;
	}
}