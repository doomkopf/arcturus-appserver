package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.appserver.system.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class ListChunkTest
{
	private static class TestServiceInternal implements InternalListChunkService<Integer>
	{
		final Map<UUID, ListChunk<Integer>> map = new HashMap<>();

		void add(Integer elemToAdd)
		{
			var chunk = map.computeIfAbsent(Constants.ZERO_UUID,
				key -> new ListChunk<>(new ArrayList<>())
			);

			chunk.add(elemToAdd, this);
		}

		@Override
		public void remove(Integer elemToRemove, UUID entityId)
		{
			var chunk = map.get(entityId);
			if (chunk != null)
			{
				chunk.remove(elemToRemove, this);
			}
		}

		@Override
		public void transferList(UUID toEntityId, UUID next, List<Integer> list)
		{
			var chunk = map.computeIfAbsent(toEntityId, key -> new ListChunk<>(new ArrayList<>()));

			chunk.init(list, next);
		}
	}

	@Test
	void test()
	{
		var fullChunksElems = ListChunk.MAX_ELEMENTS_PER_CHUNK * 2;
		var testService = new TestServiceInternal();

		for (var i = 1; i <= (fullChunksElems + 2); i++)
		{
			testService.add(i);
		}

		Assertions.assertEquals(3, testService.map.size());

		var chunk = testService.map.get(Constants.ZERO_UUID);
		Assertions.assertEquals(fullChunksElems + 1, chunk.iterator().next().intValue());

		chunk = testService.map.get(chunk.next);
		Assertions.assertEquals((fullChunksElems / 2) + 1, chunk.iterator().next().intValue());

		chunk = testService.map.get(chunk.next);
		Assertions.assertEquals(1, chunk.iterator().next().intValue());

		Assertions.assertNull(chunk.next);

		testService.remove((fullChunksElems / 2) + 1, Constants.ZERO_UUID);

		chunk = testService.map.get(Constants.ZERO_UUID);
		chunk = testService.map.get(chunk.next);
		Assertions.assertEquals((fullChunksElems / 2) + 2, chunk.iterator().next().intValue());
	}
}