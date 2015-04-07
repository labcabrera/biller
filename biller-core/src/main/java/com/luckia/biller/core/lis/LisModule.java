package com.luckia.biller.core.lis;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

public class LisModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(LisModule.class);

	@Override
	protected void configure() {
		LOG.debug("Configuring LIS module");
		bind(EntityManager.class).annotatedWith(Lis.class).toProvider(LisEntityManagerProvider.class);
	}
}
