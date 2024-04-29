package com.arcturus.appserver.system.message;

import com.arcturus.appserver.system.Constants;

import java.net.URLDecoder;

/**
 * A request from a client containing a {@link RequestHeader} and the body.
 *
 * @author doomkopf
 */
public class Request
{
	private static final String JSON_SPLIT_REGEX = "\\{";

	public static Request parseFromPayload(String payload)
	{
		payload = payload.trim();
		var headerJsonSplit = payload.split(JSON_SPLIT_REGEX, 2);
		var requestHeader = RequestHeader.parse(headerJsonSplit[0]);
		if (requestHeader == null)
		{
			return null;
		}

		return new Request(requestHeader,
			(headerJsonSplit.length > 1) ? ('{' + headerJsonSplit[1]) : null
		);
	}

	public static Request parseFromPathAndRequestBody(
		String path, String queryString, String requestBody
	)
	{
		var requestHeader = RequestHeader.parseFromPathAndQueryString(path, queryString);
		if (requestHeader == null)
		{
			return null;
		}

		if (((requestBody == null) || requestBody.isEmpty()) && (queryString != null))
		{
			requestBody = parseRequestBodyFromQueryString(queryString);
		}

		return new Request(requestHeader, requestBody);
	}

	private static String parseRequestBodyFromQueryString(String queryString)
	{
		var queryKeyValues = queryString.split("&");
		for (var queryKeyValueString : queryKeyValues)
		{
			var queryKeyValue = queryKeyValueString.split("=");
			if ("body".equalsIgnoreCase(queryKeyValue[0]))
			{
				return URLDecoder.decode(queryKeyValue[1], Constants.CHARSET_UTF8);
			}
		}

		return null;
	}

	private final RequestHeader requestHeader;
	private final String body;

	public Request(RequestHeader requestHeader, String body)
	{
		this.requestHeader = requestHeader;
		this.body = body;
	}

	public RequestHeader getRequestHeader()
	{
		return requestHeader;
	}

	public String getBody()
	{
		return body;
	}
}