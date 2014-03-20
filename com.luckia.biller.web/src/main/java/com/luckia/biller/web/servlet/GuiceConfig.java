package com.luckia.biller.web.servlet;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.luckia.biller.core.MainModule;
import com.luckia.biller.core.scheduler.SchedulerService;

public class GuiceConfig extends GuiceServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(GuiceConfig.class);

	@Override
	protected Injector getInjector() {
		LOG.debug("Configuring web application");
		Injector injector = Guice.createInjector(new RestModule(), new MainModule());
		configureScheduler(injector);
		return injector;
	}

	protected void configureScheduler(Injector injector) {
		try {
			SchedulerService schedulerService = new SchedulerService(injector);
			schedulerService.registerJobs();
			schedulerService.getScheduler().start();
		} catch (SchedulerException ex) {
			throw new RuntimeException("Error al arrancar el servicio de tareas programadas", ex);
		}
	}
}
