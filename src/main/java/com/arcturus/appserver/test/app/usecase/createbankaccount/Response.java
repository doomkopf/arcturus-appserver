package com.arcturus.appserver.test.app.usecase.createbankaccount;

import java.util.UUID;

class Response
{
	String uc = "create";
	String status;
	UUID id;

	Response(String status, UUID id)
	{
		this.status = status;
		this.id = id;
	}
}
