package com.arcturus.appserver.test.app.usecase.readbankaccount;

class Response
{
	String uc = "read";
	int money;

	public Response(int money)
	{
		this.money = money;
	}
}
