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

public class Patch20140528A extends PatchSupport implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Patch20140528A.class);

	public static void main(String[] args) {
		new Patch20140528A().run();
	}

	/**
	 * Faltan las siguientes facturas: <br>
	 * 
	 * <pre>
	 * CAMAROTE
	 * ACUARI
	 * FUTBOLEROS
	 * SALON LEYFER
	 * 
	 * <pre>
	 */
	public void run() {
		if (!confirm()) {
			System.out.println("Application aborted");
			return;
		}
		try {
			LOG.info("Ejecutando patch");
			Injector injector = Guice.createInjector(new LuckiaCoreModule());
			EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
			BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
			Date from = new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate();
			Date to = new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate();
			Range<Date> range = Range.between(from, to);
			long[] storeIds = { 2183, 2179, 2181, 2409 };
			for (long storeId : storeIds) {
				BillTask task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
				task.run();
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}
}
