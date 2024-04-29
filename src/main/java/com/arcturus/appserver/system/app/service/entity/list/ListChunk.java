package com.arcturus.appserver.system.app.service.entity.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ListChunk<T> implements Iterable<T>
{
	static final int MAX_ELEMENTS_PER_CHUNK = 64;

	private List<T> list;
	UUID next = null;

	@SuppressWarnings("unused")
	private ListChunk()
	{
	}

	public ListChunk(List<T> list)
	{
		this.list = list;
	}

	public void add(T elem, InternalListChunkService<T> listChunkService)
	{
		if (list.size() >= MAX_ELEMENTS_PER_CHUNK)
		{
			var newNext = UUID.randomUUID();
			listChunkService.transferList(newNext, next, list);
			next = newNext;
			list.clear();
		}

		list.add(elem);
	}

	public boolean remove(T elem, InternalListChunkService<T> listChunkService)
	{
		if (!list.remove(elem))
		{
			if (next != null)
			{
				listChunkService.remove(elem, next);
			}
			return false;
		}

		return true;
	}

	public void init(Collection<T> list, UUID next)
	{
		this.list.addAll(list);
		this.next = next;
	}

	public UUID getNext()
	{
		return next;
	}

	@Override
	public Iterator<T> iterator()
	{
		return list.iterator();
	}
}