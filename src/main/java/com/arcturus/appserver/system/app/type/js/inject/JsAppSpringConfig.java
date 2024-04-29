package com.arcturus.appserver.system.app.type.js.inject;

import org.springframework.context.annotation.ImportResource;

@ImportResource({
		"classpath*:/config/app_springconfig.xml",
		"classpath*:/config/js_app_springconfig.xml"})
public class JsAppSpringConfig
{
	// Nothing
}