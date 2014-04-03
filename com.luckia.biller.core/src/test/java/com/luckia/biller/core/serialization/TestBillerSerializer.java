/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;

public class TestBillerSerializer {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManager entityManager = injector.getInstance(EntityManagerProvider.class).get();
		Serializer serializer = injector.getInstance(Serializer.class);

		TypedQuery<Bill> query = entityManager.createQuery("select b from Bill b join fetch b.details order by b.code desc", Bill.class);
		Bill bill = query.setMaxResults(1).getSingleResult();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serializer.serialize(bill, out);
		byte[] raw = out.toByteArray();
		System.out.println("Serialized:\n" + new String(raw));

		Bill value = serializer.deserialize(Bill.class, new ByteArrayInputStream(raw));
		System.out.println(value);
	}
}
