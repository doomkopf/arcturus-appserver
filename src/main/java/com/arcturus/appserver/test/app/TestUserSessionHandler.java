package com.arcturus.appserver.test.app;

import java.util.UUID;

import com.arcturus.api.UserSessionHandler;

public class TestUserSessionHandler implements UserSessionHandler
{
	@Override
	public void onSessionEnded(UUID userId)
	{
		// Nothing yet
	}
}