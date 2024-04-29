package com.arcturus.appserver.system.app.type.js;

import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.Tools;

public class JsResponseSender
{
	private final ArcturusResponseSender responseSender;

	public JsResponseSender(ArcturusResponseSender responseSender)
	{
		this.responseSender = responseSender;
	}

	public void send(String requestId, String payload)
	{
		responseSender.send(Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			payload
		);
	}
}