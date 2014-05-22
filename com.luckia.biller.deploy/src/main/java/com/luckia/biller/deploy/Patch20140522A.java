package com.luckia.biller.deploy;

import java.util.Date;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.scheduler.tasks.BillTask;
import com.luckia.biller.core.services.bills.BillProcessor;

public class Patch20140522A {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		LOG.info("Ejecutando patch");
		long[] storeIds = { 2205, 2207, 2209, 2211, 2213, 2215, 2217 };
		Date from = new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate();
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		Range<Date> range = Range.between(from, to);
		EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
		for (long storeId : storeIds) {
			BillTask task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
			task.run();
		}
	}
}
