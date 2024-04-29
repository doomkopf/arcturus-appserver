package com.arcturus.appserver.system.app.service;

import com.arcturus.api.service.UseCaseHandler;

public interface UseCaseProvider
{
	interface LateBindingUseCaseProvider
	{
		void init();
	}

	UseCaseHandler getUseCaseHandler(String useCaseId);
}