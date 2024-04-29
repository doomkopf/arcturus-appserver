package com.arcturus.appserver.system.account.entity;

/**
 * An account entity containing all necessary information e.g. for logging in.
 *
 * @author doomkopf
 */
public class Account
{
	private String email;
	private String pw;

	@SuppressWarnings("unused")
	private Account()
	{
	}

	public Account(String email, String pw)
	{
		this.email = email;
		this.pw = pw;
	}

	public String getEmail()
	{
		return email;
	}

	public String getPassword()
	{
		return pw;
	}

	public void setPassword(String pw)
	{
		this.pw = pw;
	}
}
