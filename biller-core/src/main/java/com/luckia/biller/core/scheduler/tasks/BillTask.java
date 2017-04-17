package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.scheduler.BillingJob;
import com.luckia.biller.core.services.bills.BillProcessor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Componente encargado de generar una factura para un determinado establecimiento.<br>
 * El sistema ejecutara estas tareas en modo asincrono al final de cada mes.
 * 
 * @see BillingJob
 */
@Slf4j
@AllArgsConstructor
public class BillTask implements Runnable {

	private final long storeId;
	private final Range<Date> range;
	private final Provider<EntityManager> entityManagerProvider;
	private final BillProcessor billProcessor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			long t0 = System.currentTimeMillis();
			EntityManager entityManager = entityManagerProvider.get();
			Store store = entityManager.find(Store.class, storeId);
			if (store.getBillingModel() == null) {
				log.warn(
						"El establecimiento {} carece de modelo de facturacion. No se puede generar la factura",
						store.getName());
			}
			else {
				Bill bill = billProcessor.generateBill(store, range);
				billProcessor.processDetails(bill);
				billProcessor.processResults(bill);
				if (BooleanUtils.isTrue(store.getAutoConfirm())) {
					billProcessor.confirmBill(bill);
				}
				long ms = System.currentTimeMillis() - t0;
				log.debug("Procesada factura del local {} ({}) en {} ms", storeId,
						store.getName(), ms);
			}
		}
		catch (Exception ex) {
			log.error("Bill task error", ex);
		}
	}
}
