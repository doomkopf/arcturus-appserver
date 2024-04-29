package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.tool.FileReader;

import java.io.IOException;

public class ScriptEnhancer
{
	private final String enhanceCode;
	private final String toolsCode;

	public ScriptEnhancer(FileReader fileReader) throws IOException
	{
		enhanceCode = fileReader.readResourcesFile("js/enhance.js");
		toolsCode = fileReader.readResourcesFile("js/tools.js");
	}

	String enhance(String script)
	{
		script = enhanceCode + toolsCode + script;

		return script;
	}
}