package com.arcturus.appserver.system.maintainer.entity;

public class MaintainerAccount
{
	private String pw;

	@SuppressWarnings("unused")
	private MaintainerAccount()
	{
	}

	public MaintainerAccount(String password)
	{
		this.pw = password;
	}

	public String getPassword()
	{
		return pw;
	}
}