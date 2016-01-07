package com.luckia.biller.core.integration;

import org.apache.camel.guice.CamelModuleWithMatchingRoutes;

public class BillerCamelModule extends CamelModuleWithMatchingRoutes {

	@Override
	protected void configure() {
		super.configure();
		bind(BillerRouteBuilder.class);
	}
}