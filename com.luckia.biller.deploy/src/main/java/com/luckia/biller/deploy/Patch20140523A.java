package com.luckia.biller.deploy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.luckia.biller.core.scheduler.tasks.BillTask;
import com.luckia.biller.core.scheduler.tasks.LiquidationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class Patch20140523A {

	private static final Logger LOG = LoggerFactory.getLogger(Patch20140523A.class);

	public static void main(String[] args) {
		LOG.info("Ejecutando patch");
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		List<Range<Date>> ranges = new ArrayList<>();
		EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
		LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
		EntityManager entityManager = entityManagerProvider.get();

		ranges.add(Range.between(new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 2, 28, 0, 0, 0, 0).toDate()));
		ranges.add(Range.between(new DateTime(2014, 3, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 3, 31, 0, 0, 0, 0).toDate()));

		// Generamos las facturas
		if (false) {
			long[] storeIds = { 2179, 2181, 2183 };

			for (long storeId : storeIds) {
				for (Range<Date> range : ranges) {
					BillTask task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
					task.run();
				}
			}
		}
		// Regeneramos la liquidaciones (replay bares)
		String[] ids = { "53881266-7386-4a98-8b8f-de894b508152", "9a38f489-d6a5-466c-8cf3-1f168130194d" };
		for (String liquidationId : ids) {
			Liquidation liquidation = entityManagerProvider.get().find(Liquidation.class, liquidationId);
			entityManager.getTransaction().begin();
			LOG.info("Eliminando relacion de facturas con la liquidacion");
			for (Bill bill : liquidation.getBills()) {
				bill.setLiquidation(null);
				entityManager.merge(bill);
			}
			entityManager.getTransaction().commit();
			entityManager.clear();

			LOG.info("Eliminando liquidacion");
			entityManager.getTransaction().begin();
			entityManager.remove(liquidation);
			entityManager.getTransaction().commit();
		}

		long companyId = 168;
		for (Range<Date> range : ranges) {
			LiquidationTask task = new LiquidationTask(companyId, range, entityManagerProvider, liquidationProcessor);
			task.run();
		}
	}
}
