package com.luckia.biller.core.services.bills.impl;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;

public class BillCodeGeneratorTest {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		BillCodeGenerator generator = injector.getInstance(BillCodeGenerator.class);
		entityManager.getTransaction().begin();
		System.out.println(generator.generateCode("TEST/{year}/{month}/{sequence,4}"));
		System.out.println(generator.generateCode("TEST/{year}/{month}/{sequence,6}"));
		System.out.println(generator.generateCode("TEST/{year}/{month,2}/{sequence,4}"));
		System.out.println(generator.generateCode("TEST/{year}/{month, 2}/{sequence,4}"));
		entityManager.getTransaction().rollback();
	}
}
