package com.arcturus.appserver.net.jetty;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.net.HttpMethod;
import com.arcturus.appserver.net.HttpSessionListener;
import com.arcturus.appserver.system.Constants;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyHttpServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	private final Logger log;

	HttpSessionListener sessionListener;

	public JettyHttpServlet(LoggerFactory loggerFactory)
	{
		log = loggerFactory.create(getClass());
	}

	private static byte[] readBytesFromRequest(ServletRequest request) throws IOException
	{
		var contentLength = request.getContentLength();
		if (contentLength == -1)
		{
			return EMPTY_BYTE_ARRAY;
		}

		var bytes = new byte[contentLength];
		var off = 0;
		do
		{
			off += request.getInputStream().read(bytes, off, contentLength);
		}
		while (off < contentLength);

		return bytes;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		handleCatchingAllExceptions(req);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		handleCatchingAllExceptions(req);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	{
		handleCatchingAllExceptions(req);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
	{
		handleCatchingAllExceptions(req);
	}

	private void handleCatchingAllExceptions(HttpServletRequest request)
	{
		try
		{
			handle(request);
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}

	private void handle(HttpServletRequest request)
	{
		byte[] bytes;
		try
		{
			bytes = readBytesFromRequest(request);
		}
		catch (IOException e)
		{
			log.log(LogLevel.error, e);
			return;
		}

		if ((bytes.length > 0) && (bytes[0] == 0))
		{
			return;
		}

		var strPayload = new String(bytes, Constants.CHARSET_UTF8);

		var requestContext = new JettyHttpRequestContext(log, request.startAsync());

		var pathInfo = request.getPathInfo();
		if ((pathInfo != null) && (pathInfo.length() > 1))
		{
			sessionListener.onReceived(requestContext,
				request::getHeader,
				parseMethod(request.getMethod()),
				pathInfo,
				request.getQueryString(),
				strPayload
			);
		}
		else
		{
			sessionListener.onReceived(requestContext, strPayload);
		}
	}

	private static HttpMethod parseMethod(String method)
	{
		try
		{
			return HttpMethod.valueOf(method);
		}
		catch (Throwable e)
		{
			return null;
		}
	}
}