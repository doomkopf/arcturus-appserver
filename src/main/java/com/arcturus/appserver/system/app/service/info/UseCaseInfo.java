package com.arcturus.appserver.system.app.service.info;

public interface UseCaseInfo
{
	String getId();

	String getDescription();

	String getRequestBody();

	String getSuccessResponseBody();

	boolean isPublic();
}