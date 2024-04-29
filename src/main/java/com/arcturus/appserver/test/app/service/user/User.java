package com.arcturus.appserver.test.app.service.user;

import java.util.UUID;

public class User
{
	private String name;

	private UUID bankAccount1;
	private UUID bankAccount2;

	User(String name, UUID bankAccount1, UUID bankAccount2)
	{
		this.name = name;
		this.bankAccount1 = bankAccount1;
		this.bankAccount2 = bankAccount2;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public UUID getBankAccount1()
	{
		return bankAccount1;
	}

	public void setBankAccount1(UUID bankAccount1)
	{
		this.bankAccount1 = bankAccount1;
	}

	public UUID getBankAccount2()
	{
		return bankAccount2;
	}

	public void setBankAccount2(UUID bankAccount2)
	{
		this.bankAccount2 = bankAccount2;
	}
}