/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.scheduler;

import java.util.Properties;

import javax.inject.Inject;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Al crear la instancia inserta en el contexto el {@link Injector} a partir del cual podremos utilizar los servicios.
 */
@Singleton
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final Scheduler scheduler;

	@Inject
	public SchedulerService(Injector injector) {
		LOG.info("Iniciando servicio de tareas programadas");
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("/org/quartz/quartz.properties"));
			scheduler = new StdSchedulerFactory(properties).getScheduler();
			scheduler.getContext().put(Injector.class.getName(), injector);
		} catch (Exception ex) {
			throw new RuntimeException("Error al arrancar el servicio de tareas programadas", ex);
		}
	}

	/**
	 * Podemos consultar la aplicación <a href="http://www.cronmaker.com/">cronmaker.com</a> para generar las expresiones cron.
	 * @throws SchedulerException
	 */
	public void registerJobs() throws SchedulerException {
		LOG.debug("Registrando tareas programadas");
		try {
			String cron = "0 0 2 2 1/1 ? *";
			JobDetail jobDetail = JobBuilder.newJob(MailJob.class).withDescription("Mail Job").withIdentity(MailJob.class.getName()).build();
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			MutableTrigger trigger = scheduleBuilder.build();
			trigger.setKey(new TriggerKey("test"));
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (Exception ex) {
			throw new RuntimeException("Error al registrar las tareas programadas");
		}
	}

	public Scheduler getScheduler() {
		return scheduler;
	}
}
