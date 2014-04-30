package com.luckia.biller.core.scheduler.tasks;

import javax.persistence.EntityManager;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.services.bills.BillProcessor;

/**
 * Representa una tarea que recalcula los detalles de una factura.
 */
public class BillRecalculationTask implements Runnable {

	private final String billId;
	private final EntityManagerProvider entityManagerProvider;
	private final BillProcessor billProcessor;

	public BillRecalculationTask(String billId, EntityManagerProvider entityManagerProvider, BillProcessor billProcessor) {
		this.billId = billId;
		this.entityManagerProvider = entityManagerProvider;
		this.billProcessor = billProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EntityManager entityManager = entityManagerProvider.get();
		Bill bill = entityManager.find(Bill.class, billId);
		cleanPreviousResults(bill, entityManager);
		billProcessor.processDetails(bill);
		billProcessor.processResults(bill);
	}

	/**
	 * Elimina de base de datos los detalles generados anteriormente para volver a recalcular los resultados de la factura.
	 * 
	 * @param bill
	 * @param entityManager
	 */
	private void cleanPreviousResults(Bill bill, EntityManager entityManager) {
		if (bill.getDetails() != null) {
			for (Object i : bill.getDetails()) {
				entityManager.remove(i);
			}
			bill.getDetails().clear();
		}
		if (bill.getLiquidationDetails() != null) {
			for (Object i : bill.getLiquidationDetails()) {
				entityManager.remove(i);
			}
			bill.getLiquidationDetails().clear();
		}
	}
}
