package com.arcturus.appserver.net.http.client;

public class HttpResponse
{
	public int code;
	public String body;

	private HttpResponse()
	{
	}

	public HttpResponse(int code, String body)
	{
		this.code = code;
		this.body = body;
	}
}