package com.arcturus.appserver.system;

import com.arcturus.api.*;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.inject.spring.SpringConstants;
import com.arcturus.appserver.system.app.App;
import com.arcturus.appserver.system.app.AppContainer;
import com.arcturus.appserver.system.app.inject.AppSingletons;
import com.arcturus.appserver.system.app.type.java.inject.JavaAppSingletons;
import com.arcturus.appserver.system.app.type.java.inject.JavaAppSpringConfig;
import com.arcturus.appserver.system.app.type.js.inject.JsAppSingletons;
import com.arcturus.appserver.system.app.type.js.inject.JsAppSpringConfig;
import com.arcturus.appserver.system.internalapp.maintainer.MaintainerAppConfig;
import com.arcturus.appserver.system.maintainer.AppScriptEntityProvider;
import com.arcturus.appserver.system.maintainer.entity.AppScriptEntity;
import com.arcturus.appserver.test.app.TestAppConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppLoader
{
	private final Logger log;
	private final AnnotationConfigApplicationContext rootSpringContext;
	private final AppScriptEntityProvider appScriptEntityProvider;

	private final Map<String, AppConfig> appIdToJavaAppConfigMap = new HashMap<>(2);

	public AppLoader(
		LoggerFactory loggerFactory,
		AnnotationConfigApplicationContext rootSpringContext,
		Config config,
		AppConfigs appConfigs,
		AppScriptEntityProvider appScriptEntityProvider
	) throws
		InstantiationException,
		IllegalAccessException,
		InvocationTargetException,
		NoSuchMethodException,
		ClassNotFoundException
	{
		log = loggerFactory.create(getClass());
		this.rootSpringContext = rootSpringContext;
		this.appScriptEntityProvider = appScriptEntityProvider;

		// internal apps
		var testAppConfig = new TestAppConfig();
		appIdToJavaAppConfigMap.put(testAppConfig.getAppId(), testAppConfig);

		var maintainerAppConfig = new MaintainerAppConfig();
		appIdToJavaAppConfigMap.put(maintainerAppConfig.getAppId(), maintainerAppConfig);
		//

		var javaAppConfigs = config.getString(ServerConfigPropery.javaAppConfigs);
		if ((javaAppConfigs != null) && !javaAppConfigs.isEmpty())
		{
			for (var javaAppConfigClassName : javaAppConfigs.split(","))
			{
				var appConfig = (AppConfig) Class.forName(javaAppConfigClassName)
					.getDeclaredConstructor()
					.newInstance();
				appIdToJavaAppConfigMap.put(appConfig.getAppId(), appConfig);
			}
		}

		for (var appConfig : appConfigs.getAppConfigsIterable())
		{
			appIdToJavaAppConfigMap.put(appConfig.getAppId(), appConfig);
		}
	}

	AppContainer loadApp(String id) throws InterruptedException, ArcturusAppException
	{
		var javaAppConfig = appIdToJavaAppConfigMap.get(id);
		var isJavaApp = javaAppConfig != null;

		AppScriptEntity appScriptEntity = null;
		if (!isJavaApp)
		{
			try
			{
				appScriptEntity = appScriptEntityProvider.getAppScript(id, null)
					.get(4000, TimeUnit.MILLISECONDS);
				if (appScriptEntity == null)
				{
					return null;
				}
			}
			catch (ExecutionException | TimeoutException e)
			{
				throw new ArcturusAppException(e);
			}
		}

		var appContext = new AnnotationConfigApplicationContext();
		try
		{
			appContext.setParent(rootSpringContext);
			appContext.registerBeanDefinition(
				SpringConstants.BEAN_NAME_APP_SINGLETONS,
				BeanDefinitionBuilder.rootBeanDefinition(AppSingletons.class)
					.addConstructorArgValue(id)
					.getBeanDefinition()
			);

			if (isJavaApp)
			{
				appContext.registerBeanDefinition(
					SpringConstants.BEAN_NAME_JAVA_APP_SINGLETONS,
					BeanDefinitionBuilder.rootBeanDefinition(JavaAppSingletons.class)
						.addConstructorArgValue(javaAppConfig)
						.getBeanDefinition()
				);
				appContext.register(JavaAppSpringConfig.class);
			}
			else
			{
				appContext.registerBeanDefinition(
					SpringConstants.BEAN_NAME_JS_APP_SINGLETONS,
					BeanDefinitionBuilder.rootBeanDefinition(JsAppSingletons.class)
						.addConstructorArgValue(appScriptEntity.getMaintainerUserId())
						.addConstructorArgValue(appScriptEntity.getScript())
						.getBeanDefinition()
				);
				appContext.register(JsAppSpringConfig.class);
			}

			appContext.register(App.class);
			appContext.refresh();
			return new AppContainer(appContext.getBean(App.class), appContext);
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, e);
			}

			try
			{
				appContext.stop();
			}
			catch (Throwable e2)
			{
			}

			appContext.close();
		}

		return null;
	}
}