package com.arcturus.appserver.system;

import com.arcturus.appserver.system.message.AppManagementMessage;
import com.arcturus.appserver.system.message.DomainMessage;
import com.arcturus.appserver.system.message.DomainTransactionMessage;
import com.arcturus.appserver.system.message.EntityManagementMessage;
import com.arcturus.appserver.system.message.LocalManagementMessage;
import com.arcturus.appserver.system.message.ResponseOutgoingMessage;
import com.arcturus.appserver.system.message.ServiceMessage;
import com.arcturus.appserver.system.message.UserOutgoingMessage;

/**
 * Any message inside the arcturus-appserver.
 * 
 * @author doomkopf
 */
public interface Message
{
	default LocalManagementMessage<?> getLocalManagementMessage() // NOSONAR
	{
		return null;
	}

	default EntityManagementMessage getManagementMessage()
	{
		return null;
	}

	default AppManagementMessage getAppManagementMessage()
	{
		return null;
	}

	default DomainMessage getDomainMessage()
	{
		return null;
	}

	default DomainTransactionMessage getDomainTransactionMessage()
	{
		return null;
	}

	default ServiceMessage getServiceMessage()
	{
		return null;
	}

	default UserOutgoingMessage getUserOutgoingMessage()
	{
		return null;
	}

	default ResponseOutgoingMessage getResponseOutgoingMessage()
	{
		return null;
	}
}