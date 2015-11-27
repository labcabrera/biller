package com.luckia.biller.core.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCheckJob extends BaseJob {

	private static final Logger LOG = LoggerFactory.getLogger(SystemCheckJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.trace("Checking system status");
		init(context);
		// SettingsService settingsService = injector.getInstance(SettingsService.class);
		// Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
		// LiquidationMailService liquidationMailService = injector.getInstance(LiquidationMailService.class);
		// SendLiquidationsTask sendLiquidationsTask = new SendLiquidationsTask(entityManagerProvider, liquidationMailService, settingsService);
		// sendLiquidationsTask.run();
	}
}
