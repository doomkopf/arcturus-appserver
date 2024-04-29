package com.arcturus.appserver.test.app.service.person;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.appserver.system.app.type.java.JavaEntityFactory;

import java.util.UUID;

@ServiceAssignment(service = "person")
public class PersonEntityFactory implements JavaEntityFactory<Person>
{
	@Override
	public Person createDefaultEntity(UUID uuid)
	{
		return new Person("", 0);
	}

	@Override
	public int getCurrentVersion()
	{
		return 1;
	}
}