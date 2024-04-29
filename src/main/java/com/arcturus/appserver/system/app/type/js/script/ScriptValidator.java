package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class ScriptValidator
{
	private static final Collection<String> INVALID_STRINGS = Arrays.asList("java.",
		"org.",
		"com.",
		"sun."
	);

	public static class ScriptValidationResult
	{
		public final boolean ok;
		public final String message;

		ScriptValidationResult(boolean ok, String message)
		{
			this.ok = ok;
			this.message = message;
		}
	}

	private final Logger log;
	private final LoggerFactory loggerFactory;
	private final ScriptEnhancer scriptEnhancer;

	public ScriptValidator(LoggerFactory loggerFactory, ScriptEnhancer scriptEnhancer)
	{
		log = loggerFactory.create(getClass());
		this.loggerFactory = loggerFactory;
		this.scriptEnhancer = scriptEnhancer;
	}

	public void validate(String script, Consumer<ScriptValidationResult> resultConsumer)
	{
		if ((script == null) || script.trim().isEmpty())
		{
			resultConsumer.accept(new ScriptValidationResult(false, "Script is empty"));
			return;
		}

		for (var invalidString : INVALID_STRINGS)
		{
			if (script.contains(invalidString))
			{
				resultConsumer.accept(new ScriptValidationResult(false,
					"Script contains invalid strings"
				));
				return;
			}
		}

		evalScript(scriptEnhancer.enhance(script), resultConsumer);
	}

	private void evalScript(String script, Consumer<ScriptValidationResult> resultConsumer)
	{
		try (var ignored = new AppScript(loggerFactory, scriptEnhancer, script))
		{
			resultConsumer.accept(new ScriptValidationResult(true, null));
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, e);
			}

			resultConsumer.accept(new ScriptValidationResult(false, e.getMessage()));
		}
	}
}