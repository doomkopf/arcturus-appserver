package com.arcturus.appserver.test.app.service.bankaccount;

public class BankAccount
{
	private int money;

	BankAccount(int money)
	{
		this.money = money;
	}

	public int getMoney()
	{
		return money;
	}

	public void setMoney(int money)
	{
		this.money = money;
	}
}