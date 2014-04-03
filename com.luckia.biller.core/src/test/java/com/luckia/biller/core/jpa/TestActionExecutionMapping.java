/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.model.ActionExecution;

public class TestActionExecutionMapping {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();

		String uuid = UUID.randomUUID().toString();
		String data = "Frase 1\nFrase2\nFrase3";
		Date now = Calendar.getInstance().getTime();

		ActionExecution action = new ActionExecution();
		action.setId(uuid);
		action.setActionData(data);
		action.setCreated(now);
		action.setEntityClass(String.class.getName());
		action.setExecution(now);

		entityManager.getTransaction().begin();
		entityManager.persist(action);
		entityManager.getTransaction().commit();

		ActionExecution readed = entityManager.find(ActionExecution.class, uuid);
		Validate.notNull(readed);
		Validate.isTrue(readed.getActionData().equals(data));
	}
}
