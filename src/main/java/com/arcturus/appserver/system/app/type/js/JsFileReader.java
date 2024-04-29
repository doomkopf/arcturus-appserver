package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.file.Files;
import com.arcturus.appserver.system.app.inject.AppId;

import java.io.IOException;

public class JsFileReader
{
	private final Logger log;
	private final String appId;
	private final Files files;

	public JsFileReader(
		LoggerFactory loggerFactory,
		@AppId
			String appId, Files files
	)
	{
		log = loggerFactory.create(JsFileReader.class);
		this.appId = appId;
		this.files = files;
	}

	public String read(String file)
	{
		try
		{
			return files.readAppFile(appId, file);
		}
		catch (IOException e)
		{
			log.log(LogLevel.error, e);
			return null;
		}
	}
}