package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;
import com.luckia.biller.core.model.Liquidation;
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
		entityManager.lock(liquidation, LockModeType.PESSIMISTIC_READ);
		LOG.debug("Eliminando liquidacion {}", liquidation);
		LOG.info("Recalculando liquidacion de {} de {}", liquidation.getSender().getName(), liquidation.getDateTo());
		Range<Date> range = Range.between(liquidation.getDateFrom(), liquidation.getDateTo());
		Long companyId = liquidation.getSender().getId();
		Boolean currentTransaction = entityManager.getTransaction().isActive();
		if (!currentTransaction) {
			entityManager.getTransaction().begin();
		}
		entityManager.createQuery("update Bill e set e.liquidation = null where e.liquidation = :liquidation").setParameter("liquidation", liquidation).executeUpdate();
		entityManager.createQuery("delete from LiquidationDetail e where e.liquidation = :liquidation").setParameter("liquidation", liquidation).executeUpdate();
		entityManager.remove(liquidation);
		if (!currentTransaction) {
			entityManager.getTransaction().commit();
		}
		LiquidationTask task = new LiquidationTask(companyId, range, entityManagerProvider, liquidationProcessor);
		task.run();
		liquidationResult = task.getLiquidationResult();
		LOG.info("Recalculada la liquidacion de {}", liquidation.getSender().getName());

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
