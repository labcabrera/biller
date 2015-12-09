package com.luckia.biller.web.servlet;

import java.util.TimeZone;

import javax.servlet.ServletContextListener;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.luckia.biller.core.common.LiquibaseSchemaChecker;
import com.luckia.biller.core.scheduler.SchedulerService;

import liquibase.exception.LiquibaseException;

/**
 * {@link ServletContextListener} encargado de iniciar los servicios de la aplicación una vez se ha inicializado el módulo de Guice.
 */
public class GuiceServletListener extends GuiceResteasyBootstrapServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(GuiceServletListener.class);

	@Override
	protected void withInjector(Injector injector) {
		super.withInjector(injector);
		try {
			LOG.debug("User home: {}", System.getProperty("user.home"));
			LOG.debug("Time zone: {}", TimeZone.getDefault().getDisplayName());
			injector.getInstance(PersistService.class).start();
			LiquibaseSchemaChecker schemaChecker = injector.getInstance(LiquibaseSchemaChecker.class);
			schemaChecker.checkSchema();
			SchedulerService schedulerService = new SchedulerService(injector);
			schedulerService.registerJobs();
			schedulerService.getScheduler().start();
			LOG.error("Started components");
		} catch (LiquibaseException ex) {
			LOG.error("Liquibase schema initialization error", ex);
		} catch (SchedulerException ex) {
			LOG.error("Scheduler initialization error", ex);
		}
	}
}
