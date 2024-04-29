package com.arcturus.appserver.inject.spring;

import com.arcturus.appserver.inject.Injector;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * A spring based implementation of {@link Injector}.
 *
 * @author doomkopf
 */
public class SpringInjector implements Injector
{
	private final AnnotationConfigApplicationContext springContext;

	public SpringInjector(AnnotationConfigApplicationContext springContext)
	{
		this.springContext = springContext;
	}

	@Override
	public <T> T getInstance(Class<T> clazz)
	{
		springContext.register(clazz);
		return springContext.getBean(clazz);
	}
}