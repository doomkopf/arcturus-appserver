package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.tool.FileReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class JavaServiceClusterConfigProviderTest
{
	@Test
	void testGetByName() throws ArcturusAppException
	{
		var provider = new JavaServiceClusterConfigProvider("test", new FileReader()
		{
			@Override
			public String readResourcesFile(String path) throws IOException
			{
				return null;
			}

			@Override
			public List<String> readExclusiveFileLines(String path) throws IOException
			{
				return Arrays.asList(
					"user+123.212.321.100,127.0.0.1",
					"area - 10.4.2.1 ,some.the-host.com, 192.168.0.1"
				);
			}
		});

		var config = provider.getByName("user");
		Assertions.assertTrue(config.isIncludingNodes());
		Assertions.assertArrayEquals(new String[] {"123.212.321.100", "127.0.0.1"},
			config.getNodes()
		);

		config = provider.getByName("area");
		Assertions.assertFalse(config.isIncludingNodes());
		Assertions.assertArrayEquals(new String[] {"10.4.2.1", "some.the-host.com", "192.168.0.1"},
			config.getNodes()
		);

		config = provider.getByName("other");
		Assertions.assertFalse(config.isIncludingNodes());
		Assertions.assertArrayEquals(new String[] {}, config.getNodes());
	}
}