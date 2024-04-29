package com.arcturus.appserver.test.app.usecase.transfermoney;

class Response
{
	String uc = "mutationAttemptDuringTransaction";
	int moneyOfFirstAccount;

	public Response(int moneyOfFirstAccount)
	{
		this.moneyOfFirstAccount = moneyOfFirstAccount;
	}
}
