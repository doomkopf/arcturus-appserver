package com.arcturus.appserver.system.app.logmessage;

import com.arcturus.api.log.AppLogLevel;

public class LogMessage
{
	private long t;
	private AppLogLevel l;
	private String m;

	@SuppressWarnings("unused")
	private LogMessage()
	{
	}

	public LogMessage(long timestamp, AppLogLevel logLevel, String message)
	{
		t = timestamp;
		l = logLevel;
		m = message;
	}

	public long getTimestamp()
	{
		return t;
	}

	public AppLogLevel getLogLevel()
	{
		return l;
	}

	public String getMessage()
	{
		return m;
	}
}