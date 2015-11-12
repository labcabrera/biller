package com.luckia.biller.core.scheduler.tasks;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.BillDetail;
import com.luckia.biller.core.model.BillLiquidationDetail;
import com.luckia.biller.core.model.BillRawData;
import com.luckia.biller.core.model.BillingModel;
import com.luckia.biller.core.model.Store;
import com.luckia.biller.core.services.AuditService;
import com.luckia.biller.core.services.bills.BillProcessor;

/**
 * Representa una tarea que recalcula los detalles de una factura.
 */
public class BillRecalculationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BillRecalculationTask.class);

	private final String billId;
	private final Provider<EntityManager> entityManagerProvider;
	private final BillProcessor billProcessor;
	private final AuditService auditService;

	public BillRecalculationTask(String billId, Provider<EntityManager> entityManagerProvider, BillProcessor billProcessor, AuditService auditService) {
		this.billId = billId;
		this.entityManagerProvider = entityManagerProvider;
		this.billProcessor = billProcessor;
		this.auditService = auditService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EntityManager entityManager = entityManagerProvider.get();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		try {
			Bill bill = entityManager.find(Bill.class, billId);
			checkModel(bill, entityManager);
			cleanPreviousResults(bill, entityManager);
			billProcessor.processDetails(bill);
			billProcessor.processResults(bill);
			auditService.processModified(bill);
			entityManager.getTransaction().commit();
		} catch (RuntimeException ex) {
			LOG.error("Error al regenerar la factura {}", billId, ex);
			entityManager.getTransaction().rollback();
		}
	}

	/**
	 * Elimina de base de datos los detalles generados anteriormente para volver a recalcular los resultados de la factura.
	 * 
	 * @param bill
	 * @param entityManager
	 */
	private void cleanPreviousResults(Bill bill, EntityManager entityManager) {
		if (bill.getBillDetails() != null) {
			for (BillDetail detail : bill.getBillDetails()) {
				entityManager.remove(detail);
			}
			bill.getBillDetails().clear();
		}
		if (bill.getLiquidationDetails() != null) {
			for (BillLiquidationDetail i : bill.getLiquidationDetails()) {
				entityManager.remove(i);
			}
			bill.getLiquidationDetails().clear();
		}
		if (bill.getBillRawData() != null) {
			for (BillRawData i : bill.getBillRawData()) {
				entityManager.remove(i);
			}
			bill.getBillRawData().clear();
		}
		entityManager.merge(bill);
		entityManager.flush();
	}

	private void checkModel(Bill bill, EntityManager entityManager) {
		BillingModel model = bill.getSender(Store.class).getBillingModel();
		boolean update = false;
		if (bill.getModel() == null) {
			LOG.warn("La factura no esta asociada a ningun modelo de facturacion");
			if (model == null) {
				LOG.error("No se puede generar la factura. El establecimiento carece de modelo de facturacion");
			} else {
				update = true;
			}
		} else if (model != null && model.getId() != bill.getModel().getId()) {
			update = true;
		}
		if (update) {
			LOG.info("Actualizando el modelo de la factura");
			entityManager.getTransaction().begin();
			bill.setModel(model);
			entityManager.merge(bill);
			entityManager.getTransaction().commit();
		}
	}
}
