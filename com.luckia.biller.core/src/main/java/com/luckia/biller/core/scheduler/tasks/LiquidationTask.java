package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class LiquidationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationTask.class);

	private final long companyId;
	private final Injector injector;
	private final Range<Date> range;

	public LiquidationTask(long companyId, Range<Date> range, Injector injector) {
		this.companyId = companyId;
		this.range = range;
		this.injector = injector;
	}

	@Override
	public void run() {
		try {
			long t0 = System.currentTimeMillis();
			EntityManagerProvider entityManagerProvider = injector.getInstance(EntityManagerProvider.class);
			EntityManager entityManager = entityManagerProvider.get();
			Company company = entityManager.find(Company.class, companyId);
			LiquidationProcessor liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
			liquidationProcessor.processBills(company, range);
			long ms = System.currentTimeMillis() - t0;
			LOG.debug("Procesada liquidacion de la empresa {} en {} ms", company.getName(), ms);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}