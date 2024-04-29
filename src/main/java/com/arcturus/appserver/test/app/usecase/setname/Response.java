package com.arcturus.appserver.test.app.usecase.setname;

public class Response
{
	String uc = "setName";
	String status;
	String name;

	public Response(String status, String name)
	{
		this.status = status;
		this.name = name;
	}
}
