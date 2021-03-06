package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.commons.lang3.Range;

import com.google.inject.Injector;
import com.luckia.biller.core.model.Liquidation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@AllArgsConstructor
public class LiquidationRecalculationTask implements Runnable {

	private final String liquidationId;
	private final Injector injector;

	@Getter
	private Liquidation liquidationResult;

	public LiquidationRecalculationTask(String liquidationId, Injector injector) {
		this.liquidationId = liquidationId;
		this.injector = injector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		EntityManager entityManager = injector.getProvider(EntityManager.class).get();
		Boolean currentTransaction = entityManager.getTransaction().isActive();
		if (!currentTransaction) {
			entityManager.getTransaction().begin();
		}
		Liquidation liquidation = entityManager.find(Liquidation.class, liquidationId);
		entityManager.lock(liquidation, LockModeType.PESSIMISTIC_READ);
		log.debug("Eliminando liquidacion {}", liquidation);
		log.info("Recalculando liquidacion de {} de {}",
				liquidation.getSender().getName(), liquidation.getDateTo());
		Range<Date> range = Range.between(liquidation.getDateFrom(),
				liquidation.getDateTo());
		Long companyId = liquidation.getSender().getId();
		entityManager
				.createQuery(
						"update Bill e set e.liquidation = null where e.liquidation = :liquidation")
				.setParameter("liquidation", liquidation).executeUpdate();
		entityManager
				.createQuery(
						"delete from LiquidationDetail e where e.liquidation = :liquidation")
				.setParameter("liquidation", liquidation).executeUpdate();
		entityManager.flush();
		entityManager.remove(liquidation);
		if (!currentTransaction) {
			entityManager.getTransaction().commit();
		}
		LiquidationTask task = new LiquidationTask(companyId, range, injector);
		task.run();
		liquidationResult = task.getLiquidationResult();
		log.info("Recalculada la liquidacion de {}", liquidation.getSender().getName());
	}

}
