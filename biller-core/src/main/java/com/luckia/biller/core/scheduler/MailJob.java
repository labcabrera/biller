package com.luckia.biller.core.scheduler;

import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.luckia.biller.core.services.mail.MailService;

public class MailJob extends BaseJob {

	private Provider<EntityManager> entityManagerProvider;
	private MailService mailService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		entityManagerProvider = injector.getProvider(EntityManager.class);
		mailService = injector.getInstance(MailService.class);
		Validate.notNull(entityManagerProvider);
		Validate.notNull(mailService);
		System.out.println("Ejecutando job (" + new Date() + ")");
	}
}
