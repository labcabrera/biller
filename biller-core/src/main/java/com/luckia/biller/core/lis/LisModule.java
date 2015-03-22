package com.luckia.biller.core.lis;

import javax.persistence.EntityManager;

import com.google.inject.AbstractModule;

public class LisModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(EntityManager.class).annotatedWith(Lis.class).toProvider(LisEntityManagerProvider.class);
	}
}
