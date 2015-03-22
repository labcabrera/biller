package com.luckia.biller.web.servlet;

import javax.servlet.ServletContextListener;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.scheduler.SchedulerService;

/**
 * {@link ServletContextListener} encargado de iniciar los servicios de la aplicación una vez se ha inicializado el módulo de Guice.
 */
public class GuiceServletListener extends GuiceResteasyBootstrapServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(GuiceServletListener.class);

	@Override
	protected void withInjector(Injector injector) {
		super.withInjector(injector);
		try {
			injector.getInstance(PersistService.class).start();
			SchedulerService schedulerService = new SchedulerService(injector);
			schedulerService.registerJobs();
			schedulerService.getScheduler().start();
		} catch (SchedulerException ex) {
			LOG.error("Scheduler initialization error", ex);
		}
	}
}
