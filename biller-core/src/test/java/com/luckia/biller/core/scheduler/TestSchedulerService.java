package com.luckia.biller.core.scheduler;

import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.spi.MutableTrigger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.luckia.biller.core.BillerModule;

public class TestSchedulerService {

	@Test
	public void test() throws Exception {
		Injector injector = Guice.createInjector(new BillerModule());
		SchedulerService service = injector.getInstance(SchedulerService.class);

		String cron = "0 0/1 * 1/1 * ? *";
		Scheduler scheduler = service.getScheduler();
		scheduler.getContext().put(Injector.class.getName(), injector);
		JobDetail jobDetail = JobBuilder.newJob(MailJob.class).withDescription("Mail Job").withIdentity(MailJob.class.getName()).build();
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
		MutableTrigger trigger = scheduleBuilder.build();
		trigger.setKey(new TriggerKey("test"));
		scheduler.scheduleJob(jobDetail, trigger);
		scheduler.start();
		Thread.sleep(1000 * 60 * 3);
	}
}
