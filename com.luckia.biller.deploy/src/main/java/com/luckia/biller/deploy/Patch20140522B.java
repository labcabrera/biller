package com.luckia.biller.deploy;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.scheduler.tasks.LiquidationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class Patch20140522B {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final boolean PROCESS_STORES = false;
	private static final boolean PROCESS_COMPANIES = true;

	public static void main(String[] args) {
		LOG.info("Ejecutando patch");
		Date from = new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate();
		Range<Date> range = Range.between(from, to);
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
		LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
		EntityManager entityManager = entityManagerProvider.get();
		String[] ids = { "17cc6828-c27a-4850-9734-8302701f4715", "5533edd6-91c4-4f37-9972-6e8ae2932c95" };
		for (String liquidationId : ids) {
			Liquidation liquidation = entityManagerProvider.get().find(Liquidation.class, liquidationId);
			entityManager.getTransaction().begin();
			LOG.info("Eliminando relacion de facturas con la liquidacion");
			for (Bill bill : liquidation.getBills()) {
				bill.setLiquidation(null);
				entityManager.merge(bill);
			}
			entityManager.getTransaction().commit();

			LOG.info("Eliminando liquidacion");
			entityManager.getTransaction().begin();
			entityManager.remove(liquidation);
			entityManager.getTransaction().commit();
		}
		// long companyId = 168;
		// LiquidationTask task = new LiquidationTask(companyId, range, entityManagerProvider, liquidationProcessor);
		// task.run();
	}
}
