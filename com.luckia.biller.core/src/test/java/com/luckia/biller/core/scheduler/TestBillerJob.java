package com.luckia.biller.core.scheduler;

import java.util.Date;

import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.junit.Test;
import org.quartz.JobExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.LuckiaCoreModule;

public class TestBillerJob {

	@Test
	public void test() throws JobExecutionException {
		Injector injector = Guice.createInjector(new LuckiaCoreModule());
		BillingJob job = injector.getInstance(BillingJob.class);
		job.injector = injector;
		Date from = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();
		Date to = new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate();
		job.execute(Range.between(from, to), 10);
	}

}
