package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.appserver.system.message.DomainTransactionMessage;

/**
 * Helper methods for transactions.
 *
 * @author doomkopf
 */
public interface Transactions
{
	/**
	 * Checks two transactions against each other if they're about to collide in
	 * a deadlock pattern.
	 *
	 * @param incomingTransaction
	 * @param activeTransaction
	 * @param currentIndex
	 * @return True if deadlock is gonna happen.
	 */
	public static boolean checkForDeadlock(
		DomainTransactionMessage incomingTransaction,
		Transaction activeTransaction,
		int currentIndex
	)
	{
		for (var a = currentIndex + 1; a < activeTransaction.getTransactionEntities().length; a++)
		{
			for (var i = incomingTransaction.getCurrentEntityIndex() - 1; i >= 0; i--)
			{
				var activeEntity = activeTransaction.getTransactionEntities()[a];
				var incomingEntity = incomingTransaction.getTransaction()
					.getTransactionEntities()[i];
				if (activeEntity.getId().equals(incomingEntity.getId()))
				{
					return true;
				}
			}
		}

		return false;
	}
}