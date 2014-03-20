/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.deploy.dummy;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.MainModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.bills.BillProcessor;

public class CreateDummyBills implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CreateDummyBills.class);

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MainModule());
		injector.getInstance(CreateDummyBills.class).run();
	}

	@Inject
	private EntityManagerProvider entityManagerProvider;
	@Inject
	private BillProcessor billProcessor;

	@Override
	public void run() {
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.getTransaction().begin();
		deleteCurrentBills();
		entityManager.getTransaction().commit();

		Date from = new DateTime(2014, 2, 01, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 2, 28, 0, 0, 0, 0).toDate();
		createBills(Range.between(from, to));
	}

	public void deleteCurrentBills() {
		LOG.debug("Eliminando registros de facturacion");
		EntityManager entityManager = entityManagerProvider.get();
		entityManager.createQuery("delete from BillDetail");
		entityManager.createQuery("delete from Bill");
		entityManager.flush();
	}

	public void createBills(Range<Date> range) {
		LOG.debug("Generando facturas");
		EntityManager entityManager = entityManagerProvider.get();
		List<Store> stores = entityManager.createNamedQuery("Store.selectAll", Store.class).getResultList();
		Long count = 1L;
		for (Store store : stores) {
			Bill bill = billProcessor.generateBill(store, range);
			billProcessor.processDetails(bill);
			billProcessor.processResults(bill);
			LOG.debug("Generada factura {} de {} para {}", count, stores.size(), store.getName());
			count++;
		}
	}
}
