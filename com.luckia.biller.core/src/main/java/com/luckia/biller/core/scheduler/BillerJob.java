/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.scheduler;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Range;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;

/**
 * Job encargado de generar las facturas mensualmente. Recibe los siguientes parametros:
 * <ul>
 * <li><b>from</b>: fecha de inicio de facturacion</li>
 * <li><b>to</b>: fecha de final de la facturacion</li>
 * <li><b>thread.count</b>: numero de threads en los que se calcularan en paralelo las facturas. En caso de no establecerse este parametro
 * el job se ejecutara utilizando 10 threads.</li>
 * </ul>
 */
public class BillerJob extends BaseJob {

	private static final Logger LOG = LoggerFactory.getLogger(BillerJob.class);

	public static final String KEY_FROM = "from";
	public static final String KEY_TO = "to";
	public static final String KEY_THREAD_COUNT = "thread.count";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		Date from = getParameter(context, KEY_FROM, Date.class);
		Date to = getParameter(context, KEY_TO, Date.class);
		Integer threadCount = getParameter(context, KEY_THREAD_COUNT, Integer.class, 10);
		execute(Range.between(from, to), threadCount);
	}

	public void execute(Range<Date> range, Integer threadCount) throws JobExecutionException {
		EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
		EntityManager entityManager = entityManagerProvider.get();
		TypedQuery<Long> query = entityManager.createQuery("select s.id from Store s order by s.id", Long.class);
		List<Long> storeIds = query.getResultList();

		// Procesamos de forma asincrona las facturas
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		Long t0 = System.currentTimeMillis();
		for (Long storeId : storeIds) {
			BillTask task = new BillTask(storeId, range, injector);
			executorService.submit(task);
		}
		LOG.debug("Esperando a la finalizacion de {} tareas de facturacion (hilos: {})", storeIds.size(), threadCount);
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.HOURS);
			LOG.debug("Finalizadas {} tareas en {} ms", storeIds.size(), (System.currentTimeMillis() - t0));
		} catch (InterruptedException ex) {
			LOG.error("Error durante la ejecucion de las tareas", ex);
		}

		// Procesamos de forma asincrona las liquidaciones
		t0 = System.currentTimeMillis();
		executorService = Executors.newFixedThreadPool(threadCount);
		List<Company> companies = entityManager.createQuery("select c from Company c order by c.name", Company.class).getResultList();
		for (Company company : companies) {
			LiquidationTask task = new LiquidationTask(company.getId(), range, injector);
			executorService.submit(task);
		}
		LOG.debug("Esperando a la finalizacion de {} tareas de liquidacion (hilos: {})", storeIds.size(), threadCount);
		executorService.shutdown();
		try {
			executorService.awaitTermination(4, TimeUnit.HOURS);
			LOG.debug("Finalizadas {} tareas en {} ms", storeIds.size(), (System.currentTimeMillis() - t0));
		} catch (InterruptedException ex) {
			LOG.error("Error durante la ejecucion de las tareas", ex);
		}
	}
}
