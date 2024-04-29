package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.message.DomainMessage;
import com.arcturus.appserver.system.message.DomainTransactionMessage;

/**
 * Handles all domain type messages (standard and transaction).
 * 
 * @see DomainMessage
 * @see DomainTransactionMessage
 * @author doomkopf
 */
public interface DomainTypeMessageHandler
{
	void handleDomainTypeMessage(Message msg);
}