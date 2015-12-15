package com.luckia.biller.core.scheduler;

import java.io.File;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.luckia.biller.core.services.AlertService;

public class SystemCheckJob extends BaseJob {

	private static final Logger LOG = LoggerFactory.getLogger(SystemCheckJob.class);

	private AlertService alertService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.trace("Checking system status");
		init(context);
		alertService = injector.getInstance(AlertService.class);
		checkRepositoryFolder();
	}

	private void checkRepositoryFolder() {
		String repositoryPath = injector.getInstance(Key.get(String.class, Names.named("repositoryPath")));
		if (repositoryPath == null) {
			alertService.handleAlert("Missing configuration repository folder. Check app configuration");
		} else {
			File folder = new File(repositoryPath);
			if (!folder.exists()) {
				alertService.handleAlert(String.format("Missing repository folder '%s'", repositoryPath));
			} else if (!folder.canRead()) {
				alertService.handleAlert(String.format("Invalid folder permissions '%s': cant read", repositoryPath));
			} else if (folder.canWrite()) {
				alertService.handleAlert(String.format("Invalid folder permissions '%s': cant write", repositoryPath));
			}
		}
	}
}
