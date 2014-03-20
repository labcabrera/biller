package com.luckia.biller.web.servlet;

import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class RestModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("com.sun.jersey.config.property.packages", "com.luckia.biller.web.rest");
		params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
		serve("/rest/*").with(GuiceContainer.class, params);
	}
}
