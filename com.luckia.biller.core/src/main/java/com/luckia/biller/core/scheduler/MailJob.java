/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.scheduler;

import java.util.Date;

import org.apache.commons.lang3.Validate;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.services.mail.MailService;

public class MailJob extends BaseJob {

	private EntityManagerProvider entityManagerProvider;
	private MailService mailService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		mailService = injector.getInstance(MailService.class);
		Validate.notNull(entityManagerProvider);
		Validate.notNull(mailService);
		System.out.println("Ejecutando job (" + new Date() + ")");
	}
}
