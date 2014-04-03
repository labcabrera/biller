package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.scheduler.BillingJob;
import com.luckia.biller.core.services.bills.BillProcessor;

/**
 * Componente encargado de generar una factura para un determinado establecimiento.<br>
 * El sistema ejecutara estas tareas en modo asincrono al final de cada mes.
 * 
 * @see BillingJob
 */
public class BillTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BillTask.class);

	private final long storeId;
	private final Injector injector;
	private final Range<Date> range;

	public BillTask(long storeId, Range<Date> range, Injector injector) {
		this.storeId = storeId;
		this.range = range;
		this.injector = injector;
	}

	@Override
	public void run() {
		try {
			long t0 = System.currentTimeMillis();
			EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
			BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
			EntityManager entityManager = entityManagerProvider.get();
			Store store = entityManager.find(Store.class, storeId);
			Bill bill = billProcessor.generateBill(store, range);
			billProcessor.processDetails(bill);
			billProcessor.processResults(bill);
			long ms = System.currentTimeMillis() - t0;
			LOG.debug("Procesada factura del local {} ({}) en {} ms", storeId, store.getName(), ms);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
