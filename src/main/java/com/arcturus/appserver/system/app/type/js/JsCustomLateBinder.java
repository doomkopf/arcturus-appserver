package com.arcturus.appserver.system.app.type.js;

import com.arcturus.appserver.inject.Injector;
import com.arcturus.appserver.system.app.CustomLateBinder;
import com.arcturus.appserver.system.app.type.js.script.JavaObjectsContainer;
import com.arcturus.appserver.system.app.type.js.script.JavaObjectsRegistrator;

public class JsCustomLateBinder implements CustomLateBinder
{
	private final Injector appInjector;
	private final JavaObjectsRegistrator javaObjectsRegistrator;

	public JsCustomLateBinder(Injector appInjector, JavaObjectsRegistrator javaObjectsRegistrator)
	{
		this.appInjector = appInjector;
		this.javaObjectsRegistrator = javaObjectsRegistrator;
	}

	@Override
	public void lateBind()
	{
		javaObjectsRegistrator.init(appInjector.getInstance(JavaObjectsContainer.class));
	}
}