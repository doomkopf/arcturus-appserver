package com.arcturus.appserver.system.app.type.js;

import java.util.UUID;

import com.arcturus.api.UserSessionHandler;
import com.arcturus.appserver.system.app.UserSessionHandlerFactory;

public class JsUserSessionHandlerFactory implements UserSessionHandlerFactory
{
	@Override
	public UserSessionHandler create()
	{
		return new UserSessionHandler()
		{
			@Override
			public void onSessionEnded(UUID userId)
			{
				// TODO
			}
		};
	}
}