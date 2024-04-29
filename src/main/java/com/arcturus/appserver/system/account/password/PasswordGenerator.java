package com.arcturus.appserver.system.account.password;

import java.util.UUID;

public class PasswordGenerator
{
	public String generatePassword()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
