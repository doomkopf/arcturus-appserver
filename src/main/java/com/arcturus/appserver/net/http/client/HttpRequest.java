package com.arcturus.appserver.net.http.client;

public class HttpRequest
{
	public final String url;
	public final HttpClientMethod method;
	public final String body;
	public final HttpHeader[] headers;

	public HttpRequest(
		String url, HttpClientMethod method, String body, HttpHeader[] headers
	)
	{
		this.url = url;
		this.method = method;
		this.body = body;
		this.headers = headers;
	}
}