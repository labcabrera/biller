package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;

import com.google.inject.Injector;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.bills.RappelStoreProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Representa la tarea de calcular la factura de bonus de rappel para un establecimiento
 */
@Slf4j
@AllArgsConstructor
public class RappelLiquidationTask implements Runnable {

	private final long storeId;
	private final Range<Date> range;
	private final Injector injector;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			long t0 = System.currentTimeMillis();
			Provider<EntityManager> entityManagerProvider = injector
					.getProvider(EntityManager.class);
			EntityManager entityManager = entityManagerProvider.get();
			RappelStoreProcessor rappelProcessor = injector
					.getInstance(RappelStoreProcessor.class);
			Store store = entityManager.find(Store.class, storeId);
			rappelProcessor.processRappel(store, range);
			long ms = System.currentTimeMillis() - t0;
			log.debug("Procesado rappel del establecimiento {} en {} ms", store.getName(),
					ms);
		}
		catch (Exception ex) {
			log.error("Rappel liquidation task error", ex);
		}
	}

}
