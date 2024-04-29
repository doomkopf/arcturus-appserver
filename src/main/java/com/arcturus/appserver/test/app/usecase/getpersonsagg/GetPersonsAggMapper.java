package com.arcturus.appserver.test.app.usecase.getpersonsagg;

import com.arcturus.api.service.entity.aggregation.MappingEntityUseCase;
import com.arcturus.api.service.entity.aggregation.PojoMappingEntityUseCaseHandler;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.service.person.Person;

import java.util.UUID;

@MappingEntityUseCase(service = "person", id = "map")
public class GetPersonsAggMapper extends PojoMappingEntityUseCaseHandler<Person, PersonAggregate>
{
	public GetPersonsAggMapper(JsonStringSerializer jsonStringSerializer)
	{
		super(jsonStringSerializer);
	}

	@Override
	protected PersonAggregate mapToPojo(Person entity, UUID id)
	{
		return new PersonAggregate(id, entity.getName(), entity.getAge());
	}
}