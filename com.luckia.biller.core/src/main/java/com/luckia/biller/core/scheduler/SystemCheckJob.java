package com.luckia.biller.core.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCheckJob extends BaseJob {

	private static final Logger LOG = LoggerFactory.getLogger(SystemCheckJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.debug("Checking system status");
	}

}
