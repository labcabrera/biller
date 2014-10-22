package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;
import java.util.Iterator;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

/**
 * Representa la tarea de recalcular una determinada liquidacion. El proceso que sigue es:
 * <ul>
 * <li>Elimina la relacion de las facturas con la liquidacion</li>
 * <li>Elimina los liquidationDetails asociados a la liquidacion</li>
 * <li>Elimina la liquidacion</li>
 * <li>Genera una nueva liquidacion a partir de @{link LiquidationTask}</li>
 * </ul>
 * 
 * @see LiquidationTask
 */
public class LiquidationRecalculationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationRecalculationTask.class);

	private final String liquidationId;
	private final Provider<EntityManager> entityManagerProvider;
	private final LiquidationProcessor liquidationProcessor;
	private Liquidation liquidationResult;

	public LiquidationRecalculationTask(String liquidationId, Provider<EntityManager> entityManagerProvider, LiquidationProcessor liquidationProcessor) {
		this.liquidationId = liquidationId;
		this.entityManagerProvider = entityManagerProvider;
		this.liquidationProcessor = liquidationProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EntityManager entityManager = entityManagerProvider.get();
		Liquidation liquidation = entityManagerProvider.get().find(Liquidation.class, liquidationId);
		LOG.info("Recalculando liquidacion de {} de {}", liquidation.getSender().getName(), liquidation.getDateTo());
		LOG.info("Eliminando relacion de facturas asociadas a la liquidacion");
		Range<Date> range = Range.between(liquidation.getDateFrom(), liquidation.getDateTo());
		Long companyId = liquidation.getSender().getId();
		entityManager.getTransaction().begin();
		for (Bill bill : liquidation.getBills()) {
			bill.setLiquidation(null);
			entityManager.merge(bill);
		}
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
		LOG.info("Eliminando LiquidationDetails asociados");
		for (Iterator<LiquidationDetail> iterator = liquidation.getDetails().iterator(); iterator.hasNext();) {
			LiquidationDetail i = iterator.next();
			entityManager.remove(i);
			iterator.remove();
		}
		entityManager.getTransaction().commit();
		LOG.info("Eliminando liquidacion");
		entityManager.getTransaction().begin();
		entityManager.remove(liquidation);
		entityManager.getTransaction().commit();
		LiquidationTask task = new LiquidationTask(companyId, range, entityManagerProvider, liquidationProcessor);
		task.run();
		liquidationResult = task.getLiquidationResult();
	}

	/**
	 * Guarda la liquidacion generada una vez se ha ejecutado el proceso.
	 * 
	 * @return
	 */
	public Liquidation getLiquidationResult() {
		return liquidationResult;
	}
}
