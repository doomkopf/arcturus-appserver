package com.arcturus.appserver.net.http.client;

public class HttpClientResponse
{
	public String payload;
	public HttpResponse httpResponse;

	private HttpClientResponse()
	{
	}

	public HttpClientResponse(
		String payload, HttpResponse httpResponse
	)
	{
		this.payload = payload;
		this.httpResponse = httpResponse;
	}
}