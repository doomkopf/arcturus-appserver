package com.arcturus.appserver.system.message;

import com.arcturus.appserver.system.SerializableMessage;

/**
 * Flag added to {@link SerializableMessage}s to deserialize the right type.
 * 
 * @author doomkopf
 */
public enum SerializableMessageType
{
	entityService
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new ServiceMessage();
		}

	},
	domain
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new DomainMessage();
		}
	},
	domainTransaction
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new DomainTransactionMessage();
		}
	},
	userOutgoing
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new UserOutgoingMessage();
		}
	},
	responseOutgoing
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new ResponseOutgoingMessage();
		}
	},
	entityManagement
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new EntityManagementMessage();
		}
	},
	appManagement
	{
		@Override
		public SerializableMessage createMessage()
		{
			return new AppManagementMessage();
		}
	};

	public abstract SerializableMessage createMessage();
}