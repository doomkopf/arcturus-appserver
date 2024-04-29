package com.arcturus.appserver.system.account.login;

import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.account.login.LoginManager.LoginCallback;

public interface LoginHandler<R>
{
	Class<R> requestType();

	void handle(
		R request,
		String appId,
		String useCaseId,
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		LoginCallback loginCallback
	);
}