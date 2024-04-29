package com.arcturus.appserver.test.app.usecase.getpersons;

import java.util.Collection;

class Response
{
	String uc = "getAllPersons";
	Collection<String> names;

	Response(Collection<String> names)
	{
		this.names = names;
	}
}