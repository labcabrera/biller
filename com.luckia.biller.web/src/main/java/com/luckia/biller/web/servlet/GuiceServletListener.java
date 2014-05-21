package com.luckia.biller.web.servlet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.Range;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.joda.time.DateTime;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.common.Message;
import com.luckia.biller.core.scheduler.SchedulerService;
import com.luckia.biller.core.scheduler.tasks.BillTask;
import com.luckia.biller.core.scheduler.tasks.LiquidationTask;
import com.luckia.biller.core.services.bills.BillProcessor;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

/**
 * {@link ServletContextListener} encargado de iniciar los servicios de la aplicación una vez se ha inicializado el módulo de Guice.
 */
public class GuiceServletListener extends GuiceResteasyBootstrapServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(GuiceServletListener.class);

	@Override
	protected void withInjector(Injector injector) {
		super.withInjector(injector);
		try {
			SchedulerService schedulerService = new SchedulerService(injector);
			schedulerService.registerJobs();
			schedulerService.getScheduler().start();
		} catch (SchedulerException ex) {
			LOG.error("Scheduler initialization error", ex);
		}
		// try {
		// execute(injector);
		// } catch (Exception ex) {
		// LOG.error(ex.getMessage(), ex);
		// }
	}

	// public synchronized Message<String> execute(Injector injector) {
	// LOG.info("--------------------------- EJECUTANDO PATCH ---------------------------");
	// EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
	// BillProcessor billProcessor = injector.getInstance(BillProcessor.class);
	// LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
	// List<Range<Date>> ranges = new ArrayList<>();
	// ranges.add(Range.between(new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 1, 31, 0, 0, 0, 0).toDate()));
	// ranges.add(Range.between(new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 2, 28, 0, 0, 0, 0).toDate()));
	// ranges.add(Range.between(new DateTime(2014, 3, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 3, 31, 0, 0, 0, 0).toDate()));
	// ranges.add(Range.between(new DateTime(2014, 4, 1, 0, 0, 0, 0).toDate(), new DateTime(2014, 4, 30, 0, 0, 0, 0).toDate()));
	//
	// EntityManager entityManager = entityManagerProvider.get();
	// TypedQuery<Company> queryCompanies =
	// entityManager.createQuery("select c from Company c where c.name like :name1 or c.name like :name2", Company.class);
	// List<Company> companies = queryCompanies.setParameter("name1", "%Replay%").setParameter("name2", "%Videomani%").getResultList();
	// LOG.debug("Encontradas {} empresas", companies.size());
	//
	// TypedQuery<Long> query = entityManager.createQuery("select s.id from Store s where s.parent in :companies", Long.class);
	// query.setParameter("companies", companies);
	// List<Long> storeIds = query.getResultList();
	// LOG.debug("Encontrados {} establecimientos", storeIds.size());
	//
	// LOG.debug("------------------------- regenerando facturas ------------------------");
	//
	// for (Range<Date> range : ranges) {
	// LOG.debug("------------------------- rango {} {} ------------------------", range.getMinimum(), range.getMaximum());
	// // Procesamos de forma asincrona las facturas
	// for (Long storeId : storeIds) {
	// BillTask task = new BillTask(storeId, range, entityManagerProvider, billProcessor);
	// task.run();
	// }
	// }
	//
	// LOG.debug("------------------------- regenerando liquidaciones ------------------------");
	// try {
	// Thread.sleep(30000);
	// } catch (InterruptedException ex) {
	// }
	//
	// for (Company company : companies) {
	// // Step 3 : regeneramos las liquidaciones
	// for (Range<Date> range : ranges) {
	// LOG.debug("------------------------- rango {} {} ------------------------", range.getMinimum(), range.getMaximum());
	// LiquidationTask task = new LiquidationTask(company.getId(), range, entityManagerProvider, liquidationProcessor);
	// task.run();
	// }
	// }
	//
	// return new Message<String>(Message.CODE_SUCCESS, "Ejecutado patch");
	// }
}
