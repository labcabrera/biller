/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.StateDefinition;

public class TestStateDefinitionConverter {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();
		List<StateDefinition> list = entityManager.createQuery("select a from StateDefinition a", StateDefinition.class).getResultList();
		System.out.println(list);
	}

}
