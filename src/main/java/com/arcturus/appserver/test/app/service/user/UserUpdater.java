package com.arcturus.appserver.test.app.service.user;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.api.service.entity.EntityUpdater;
import com.arcturus.api.service.entity.UseCaseContext;

import java.util.UUID;

@ServiceAssignment(service = "user")
public class UserUpdater implements EntityUpdater<User>
{
	@Override
	public void update(
		User entity, UUID id, long currentTimeMillis, long deltaTime, UseCaseContext useCaseContext
	)
	{
		// Nothing
	}
}