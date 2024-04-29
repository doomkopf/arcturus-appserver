package com.arcturus.appserver.test.app.service.bankaccount;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.appserver.system.app.type.java.JavaEntityFactory;

import java.util.UUID;

@ServiceAssignment(service = "bankAccount")
public class BankAccountEntityFactory implements JavaEntityFactory<BankAccount>
{
	@Override
	public BankAccount createDefaultEntity(UUID id)
	{
		return new BankAccount(0);
	}

	@Override
	public int getCurrentVersion()
	{
		return 1;
	}
}