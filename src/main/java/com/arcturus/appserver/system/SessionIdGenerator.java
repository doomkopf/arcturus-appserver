package com.arcturus.appserver.system;

import java.util.concurrent.ThreadLocalRandom;

public class SessionIdGenerator
{
	private final IdGenerator idGenerator;

	public SessionIdGenerator(IdGenerator idGenerator)
	{
		this.idGenerator = idGenerator;
	}

	public long generate()
	{
		return idGenerator.generate() * ThreadLocalRandom.current().nextInt();
	}
}
