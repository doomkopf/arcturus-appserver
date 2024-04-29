package com.arcturus.appserver.file;

import com.arcturus.api.tool.FileReader;
import com.arcturus.appserver.system.Constants;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Some helper methods for dealing with files.
 *
 * @author doomkopf
 */
public class Files implements FileReader
{
	@Override
	public String readResourcesFile(String path) throws IOException
	{
		String content;
		try
		{
			content = java.nio.file.Files.readString(Paths.get(path), Constants.CHARSET_UTF8);
		}
		catch (IOException e)
		{
			content = null;
		}

		if (content == null)
		{
			content = Resources.asCharSource(Resources.getResource(path), Constants.CHARSET_UTF8)
				.read();
		}

		return content;
	}

	@Override
	public List<String> readExclusiveFileLines(String path) throws IOException
	{
		return java.nio.file.Files.readAllLines(Paths.get(path), Constants.CHARSET_UTF8);
	}

	public String readAppFile(String appId, String file) throws IOException
	{
		return java.nio.file.Files.readString(Paths.get("files/" + appId + '/' + file),
			Constants.CHARSET_UTF8
		);
	}
}