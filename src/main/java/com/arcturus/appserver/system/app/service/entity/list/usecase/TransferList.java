package com.arcturus.appserver.system.app.service.entity.list.usecase;

import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.ListElementTypeSerializer;

import java.util.ArrayList;
import java.util.UUID;

public class TransferList<T> implements EntityUseCaseHandler<ListChunk<T>>
{
	private final JsonFactory jsonFactory;
	private final ListElementTypeSerializer<T> serializer;

	public TransferList(JsonFactory jsonFactory, ListElementTypeSerializer<T> serializer)
	{
		this.jsonFactory = jsonFactory;
		this.serializer = serializer;
	}

	@Override
	public void handle(
		ListChunk<T> entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		String payload,
		UseCaseContext context
	)
	{
		var json = jsonFactory.parse(payload);

		UUID next = null;
		if (json.has("n"))
		{
			next = UUID.fromString(json.getString("n"));
		}

		var jsonList = json.getArray("l");
		var list = new ArrayList<T>(jsonList.length());
		for (var i = 0; i < jsonList.length(); i++)
		{
			list.add(serializer.elementFromString(jsonList.getString(i)));
		}

		entity.init(list, next);
		context.dirty();
	}
}