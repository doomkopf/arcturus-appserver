package com.arcturus.appserver.api;

import com.arcturus.api.AppConfig;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.inject.spring.SpringConstants;
import com.arcturus.appserver.system.AppConfigs;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;

/**
 * Starts the arcturus-appserver.
 *
 * @author doomkopf
 */
@Configuration
@ImportResource("classpath*:/config/root_springconfig.xml")
public interface Startup
{
	static ArcturusApi start(Config config, AppConfig... appConfig)
	{
		var context = new AnnotationConfigApplicationContext();
		context.register(Startup.class);

		if (config == null)
		{
			config = new Config();
		}
		context.getBeanFactory().registerSingleton(SpringConstants.BEAN_NAME_CONFIG, config);

		context.registerBeanDefinition(
			SpringConstants.BEAN_NAME_APP_CONFIGS,
			BeanDefinitionBuilder.rootBeanDefinition(AppConfigs.class)
				.addConstructorArgValue(Arrays.asList(appConfig))
				.getBeanDefinition()
		);

		context.registerShutdownHook();
		context.refresh();

		context.register(ArcturusApi.class);
		return context.getBean(ArcturusApi.class);
	}

	/**
	 * @return ArcturusApi
	 */
	static ArcturusApi start(AppConfig... appConfig)
	{
		return start(null, appConfig);
	}
}