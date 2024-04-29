package com.arcturus.appserver.test.app.usecase.getpersonsagg;

import java.util.UUID;

public class PersonAggregate
{
	private UUID id;
	private String name;
	private int age;

	private PersonAggregate()
	{
	}

	public PersonAggregate(UUID id, String name, int age)
	{
		this.id = id;
		this.name = name;
		this.age = age;
	}

	public String getName()
	{
		return name;
	}

	public int getAge()
	{
		return age;
	}
}