package com.luckia.biller.core.scheduler.tasks;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckia.biller.core.jpa.EntityManagerProvider;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.services.bills.LiquidationProcessor;

/**
 * Tarea encargada de realizar la liquidación (o liquidaciones en el caso de que hubiera más de un centro de coste) de un operador.
 * 
 * @see LiquidationProcessor
 */
public class LiquidationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LiquidationTask.class);

	private final long companyId;
	private final Range<Date> range;
	private final EntityManagerProvider entityManagerProvider;
	private final LiquidationProcessor liquidationProcessor;
	private Liquidation liquidationResult;

	public LiquidationTask(long companyId, Range<Date> range, EntityManagerProvider entityManagerProvider, LiquidationProcessor liquidationProcessor) {
		this.companyId = companyId;
		this.range = range;
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
		try {
			long t0 = System.currentTimeMillis();
			EntityManager entityManager = entityManagerProvider.get();
			Company company = entityManager.find(Company.class, companyId);
			liquidationResult = liquidationProcessor.processBills(company, range);
			if (BooleanUtils.isTrue(company.getAutoConfirm())) {
				liquidationProcessor.confirm(liquidationResult);
			}
			long ms = System.currentTimeMillis() - t0;
			LOG.debug("Procesada liquidacion de la empresa {} de {} en {} ms", company.getName(), ISO_DATE_FORMAT.format(range.getMaximum()), ms);
		} catch (Exception ex) {
			LOG.error("Error al procesar la liquidacion", ex);
		}
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