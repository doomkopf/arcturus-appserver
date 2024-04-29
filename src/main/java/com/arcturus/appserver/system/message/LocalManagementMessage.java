package com.arcturus.appserver.system.message;

import java.util.function.Consumer;

import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.app.service.entity.EntityNanoProcess;

/**
 * A message that is only sent inside one VM containing the behavior directly.
 * 
 * @author doomkopf
 */
public class LocalManagementMessage<E> implements Message
{
	private final Consumer<EntityNanoProcess<E>> behavior;

	public LocalManagementMessage(Consumer<EntityNanoProcess<E>> behavior)
	{
		this.behavior = behavior;
	}

	public void execute(EntityNanoProcess<E> nanoProcess)
	{
		behavior.accept(nanoProcess);
	}

	@Override
	public LocalManagementMessage<?> getLocalManagementMessage()
	{
		return this;
	}
}