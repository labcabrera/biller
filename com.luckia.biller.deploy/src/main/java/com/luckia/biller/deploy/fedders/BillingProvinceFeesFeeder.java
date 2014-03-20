/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.fedders;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.BillingProvinceFees;
import com.luckia.biller.core.model.Province;

public class BillingProvinceFeesFeeder implements Feeder<BillingProvinceFees> {

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Override
	public void loadEntities(InputStream source) {
		EntityManager entityManager = entityManagerProvider.get();
		List<Province> provinces = entityManager.createQuery("select p from Province p", Province.class).getResultList();
		for (Province province : provinces) {
			BillingProvinceFees fees = new BillingProvinceFees();
			fees.setFeesPercent(new BigDecimal("10.00"));
			fees.setProvince(province);
			entityManager.persist(fees);
		}
	}
}
