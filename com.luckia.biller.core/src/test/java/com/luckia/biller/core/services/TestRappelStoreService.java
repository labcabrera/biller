package com.luckia.biller.core.services;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;
import com.luckia.biller.core.scheduler.RappelLiquidationJob;

public class TestRappelStoreService {

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		RappelLiquidationJob job = injector.getInstance(RappelLiquidationJob.class);
		job.setInjector(injector);
		Date date = new DateTime(2014, 12, 31, 0, 0, 0, 0).toDate();
		job.execute(date, 1);
	}
}
