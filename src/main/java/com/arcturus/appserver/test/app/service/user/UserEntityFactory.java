package com.arcturus.appserver.test.app.service.user;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.appserver.system.app.type.java.JavaEntityFactory;

import java.util.UUID;

@ServiceAssignment(service = "user")
public class UserEntityFactory implements JavaEntityFactory<User>
{
	@Override
	public User createDefaultEntity(UUID id)
	{
		return new User("", null, null);
	}

	@Override
	public int getCurrentVersion()
	{
		return 1;
	}
}