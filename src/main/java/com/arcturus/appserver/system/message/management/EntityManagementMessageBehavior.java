package com.arcturus.appserver.system.message.management;

import com.arcturus.appserver.system.app.service.entity.EntityNanoProcess;
import com.arcturus.appserver.system.message.EntityManagementMessage;

/**
 * Various behavior definitions for {@link EntityManagementMessage}s.
 *
 * @author doomkopf
 */
public enum EntityManagementMessageBehavior
{
	kill
		{
			@Override
			public void execute(EntityManagementMessage msg, EntityNanoProcess<?> proc)
			{
				proc.shutdown(false);
			}
		};

	/**
	 * Called in the procs context
	 */
	public abstract void execute(EntityManagementMessage msg, EntityNanoProcess<?> proc);
}