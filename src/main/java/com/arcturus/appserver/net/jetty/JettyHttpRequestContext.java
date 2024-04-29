package com.arcturus.appserver.net.jetty;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.appserver.net.HttpConstants;
import com.arcturus.appserver.net.RequestContext;

import javax.servlet.AsyncContext;
import java.io.IOException;

/**
 * Jetty based implementation of a {@link RequestContext}.
 *
 * @author doomkopf
 */
public class JettyHttpRequestContext implements RequestContext
{
	private final Logger log;
	private final AsyncContext asyncContext;

	public JettyHttpRequestContext(Logger log, AsyncContext asyncContext)
	{
		this.log = log;
		this.asyncContext = asyncContext;
	}

	@Override
	public void respond(String jsonString)
	{
		var response = asyncContext.getResponse();
		response.setContentType(HttpConstants.CONTENT_TYPE_JSON);
		try
		{
			response.getWriter().print(jsonString);
		}
		catch (IOException e)
		{
			log.log(LogLevel.error, e);
		}

		asyncContext.complete();

		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(LogLevel.debug, "Sent payload: " + jsonString);
		}
	}

	@Override
	public String getIp()
	{
		return asyncContext.getRequest().getRemoteAddr();
	}
}