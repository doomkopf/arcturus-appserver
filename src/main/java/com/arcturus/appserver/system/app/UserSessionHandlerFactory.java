package com.arcturus.appserver.system.app;

import com.arcturus.api.UserSessionHandler;

public interface UserSessionHandlerFactory
{
	UserSessionHandler create();
}