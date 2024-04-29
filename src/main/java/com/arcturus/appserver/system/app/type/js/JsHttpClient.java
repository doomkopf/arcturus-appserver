package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.http.client.ArcturusHttpClient;
import com.arcturus.appserver.net.http.client.HttpClientMethod;
import com.arcturus.appserver.net.http.client.HttpHeader;
import com.arcturus.appserver.net.http.client.HttpRequest;
import com.arcturus.appserver.system.Tools;

import java.util.UUID;

public class JsHttpClient
{
	private static class Headers
	{
		HttpHeader[] headers;
	}

	private final ArcturusHttpClient httpClient;
	private final JsonStringSerializer jsonStringSerializer;

	public JsHttpClient(
		ArcturusHttpClient httpClient, JsonStringSerializer jsonStringSerializer
	)
	{
		this.httpClient = httpClient;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public void request(
		String url,
		int method,
		String body,
		String headersJson,
		String resultFunc,
		String requestId,
		String requestingUserId,
		String payload
	)
	{
		Headers headers = jsonStringSerializer.fromJsonString(Headers.class, headersJson);

		httpClient.request(
			new HttpRequest(url, HttpClientMethod.values()[method], body, headers.headers),
			resultFunc,
			Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			(requestingUserId == null) ? null : UUID.fromString(requestingUserId),
			payload
		);
	}
}