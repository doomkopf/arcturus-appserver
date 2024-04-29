package com.arcturus.appserver.system.internalapp.maintainer.usecase.openapplog;

import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.app.logmessage.LogMessage;

import java.util.List;

public class OpenAppLogResponse
{
	NetStatusCode status;
	final String uc = InternalUseCases.OPEN_APP_LOG;
	List<LogMessage> messages;

	public OpenAppLogResponse(NetStatusCode status, List<LogMessage> messages)
	{
		this.status = status;
		this.messages = messages;
	}
}