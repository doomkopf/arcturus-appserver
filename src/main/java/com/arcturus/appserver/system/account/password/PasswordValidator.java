package com.arcturus.appserver.system.account.password;

public class PasswordValidator
{
	private static final int MIN_PASSWORD_LENGTH = 3;
	private static final int MAX_PASSWORD_LENGTH = 32;

	public boolean validatePassword(String password)
	{
		return (password.length() >= MIN_PASSWORD_LENGTH) && (password.length()
			<= MAX_PASSWORD_LENGTH);
	}
}
