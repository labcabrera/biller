package com.luckia.biller.core.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.BillerModule;
import com.luckia.biller.core.model.StateDefinition;

@Ignore
public class TestStateDefinitionConverter {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new BillerModule());
		injector.getInstance(PersistService.class).start();
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		List<StateDefinition> list = entityManager.createQuery("select a from StateDefinition a", StateDefinition.class).getResultList();
		System.out.println(list);
	}
}
