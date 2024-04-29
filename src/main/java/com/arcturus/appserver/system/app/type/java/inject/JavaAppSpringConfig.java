package com.arcturus.appserver.system.app.type.java.inject;

import org.springframework.context.annotation.ImportResource;

@ImportResource({
		"classpath*:/config/app_springconfig.xml",
		"classpath*:/config/java_app_springconfig.xml",
		"classpath*:/config/spring/beans.xml"})
public class JavaAppSpringConfig
{
	// Nothing
}