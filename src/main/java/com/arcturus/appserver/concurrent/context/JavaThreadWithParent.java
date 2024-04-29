package com.arcturus.appserver.concurrent.context;

public class JavaThreadWithParent extends Thread implements ContextExecutableThread
{
	private final ContextExecutableThread parent;

	public JavaThreadWithParent(ContextExecutableThread parent, Runnable target, String name)
	{
		super(target, name);
		this.parent = parent;
	}

	@Override
	public void execute(
		Runnable runnable, Callback callback
	)
	{
		parent.execute(runnable, callback);
	}
}