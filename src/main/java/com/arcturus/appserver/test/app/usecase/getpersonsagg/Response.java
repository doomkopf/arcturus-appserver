package com.arcturus.appserver.test.app.usecase.getpersonsagg;

import java.util.Collection;

class Response
{
	String uc = "getPersonsAgg";
	Collection<String> names;

	Response(Collection<String> names)
	{
		this.names = names;
	}
}