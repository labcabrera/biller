package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;

/**
 * Representa la tarea de calcular la factura de bonus de rappel para un establecimiento
 */
public class RappelLiquidationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(RappelLiquidationTask.class);

	private final long storeId;
	private final Injector injector;
	private final Range<Date> range;

	public RappelLiquidationTask(long storeId, Range<Date> range, Injector injector) {
		this.storeId = storeId;
		this.range = range;
		this.injector = injector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			long t0 = System.currentTimeMillis();
			Provider<EntityManager> entityManagerProvider = injector.getProvider(EntityManager.class);
			EntityManager entityManager = entityManagerProvider.get();
			RappelStoreProcessor rappelProcessor = injector.getInstance(RappelStoreProcessor.class);
			Store store = entityManager.find(Store.class, storeId);
			rappelProcessor.processRappel(store, range);
			long ms = System.currentTimeMillis() - t0;
			LOG.debug("Procesado rappel del establecimiento {} en {} ms", store.getName(), ms);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
