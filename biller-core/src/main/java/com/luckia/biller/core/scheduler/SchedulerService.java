/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.scheduler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
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
import com.luckia.biller.core.model.AppSettings;
import com.luckia.biller.core.services.SettingsService;

/**
 * Al crear la instancia inserta en el contexto el {@link Injector} a partir del cual podremos utilizar los servicios.
 */
@Singleton
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final SettingsService settingsService;
	private final Scheduler scheduler;

	/**
	 * Constructor de la clase que genera la instancia del Scheduler de Quartz.
	 * 
	 * @param injector
	 */
	@Inject
	public SchedulerService(Injector injector) {
		LOG.info("Iniciando servicio de tareas programadas");
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("/org/quartz/quartz.properties"));
			scheduler = new StdSchedulerFactory(properties).getScheduler();
			scheduler.getContext().put(Injector.class.getName(), injector);
			settingsService = injector.getInstance(SettingsService.class);
		} catch (Exception ex) {
			throw new RuntimeException("Error al arrancar el servicio de tareas programadas", ex);
		}
	}

	/**
	 * Podemos consultar la aplicación <a href="http://www.cronmaker.com/">cronmaker.com</a> para generar las expresiones cron.
	 * 
	 * @throws SchedulerException
	 */
	public void registerJobs() throws SchedulerException {
		LOG.debug("Registrando tareas programadas");
		try {
			AppSettings systemSettings = settingsService.getSystemSettings();
			Map<String, Class<? extends Job>> map = new LinkedHashMap<>();
			map.put(systemSettings.getValue("job.biller.cron", String.class), BillingJob.class);
			map.put(systemSettings.getValue("job.rappel.cron", String.class), RappelLiquidationJob.class);
			map.put(systemSettings.getValue("job.system.check.cron", String.class), SystemCheckJob.class);
			for (String cron : map.keySet()) {
				Class<? extends Job> jobClass = map.get(cron);
				registerJob(jobClass.getSimpleName(), cron, jobClass);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error al registrar las tareas programadas", ex);
		}
	}

	private void registerJob(String name, String cronExpression, Class<? extends Job> type) throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob(type).withDescription(name).withIdentity(type.getName()).build();
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
		MutableTrigger trigger = scheduleBuilder.build();
		trigger.setKey(new TriggerKey(name));
		scheduler.scheduleJob(jobDetail, trigger);
	}

	public Scheduler getScheduler() {
		return scheduler;
	}
}
