package com.luckia.biller.core.scheduler;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.junit.Test;
import org.quartz.JobExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;

public class TestBillerJob {

	@Test
	public void test() throws JobExecutionException {
		Injector injector = Guice.createInjector(new BillerModule());
		BillingJob job = injector.getInstance(BillingJob.class);
		job.injector = injector;

		int monthFrom = 1;
		int monthTo = 7;

		for (int month = monthFrom; month <= monthTo; month++) {
			DateTime from = new DateTime(2014, month, 1, 0, 0, 0, 0);
			DateTime to = from.dayOfMonth().withMaximumValue();
			job.execute(Range.between(from.toDate(), to.toDate()), 10);
		}
	}
}
