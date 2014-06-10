package com.luckia.biller.core.scheduler.tasks;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Bill;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.model.LiquidationDetail;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

public class LiquidationRecalculationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationRecalculationTask.class);

	private final String liquidationId;
	private final EntityManagerProvider entityManagerProvider;
	private final LiquidationProcessor liquidationProcessor;
	private Liquidation liquidationResult;

	public LiquidationRecalculationTask(String liquidationId, EntityManagerProvider entityManagerProvider, LiquidationProcessor liquidationProcessor) {
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
		for (LiquidationDetail i : liquidation.getDetails()) {
			i.setLiquidation(null);
			entityManager.merge(i);
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
