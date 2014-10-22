package com.luckia.biller.core.scheduler;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.scheduler.tasks.SendLiquidationsTask;
import com.luckia.biller.core.services.LiquidationMailService;
import com.luckia.biller.core.services.SettingsService;

public class SystemCheckJob extends BaseJob {

	private static final Logger LOG = LoggerFactory.getLogger(SystemCheckJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.trace("Checking system status");
		init(context);
		Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
		LiquidationMailService liquidationMailService = injector.getInstance(LiquidationMailService.class);
		SettingsService settingsService = injector.getInstance(SettingsService.class);
		SendLiquidationsTask sendLiquidationsTask = new SendLiquidationsTask(entityManagerProvider, liquidationMailService, settingsService);
		sendLiquidationsTask.run();
	}
}
