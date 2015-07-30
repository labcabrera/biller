package com.luckia.biller.core.scheduler.tasks;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.luckia.biller.core.model.AppFile;
import com.luckia.biller.core.model.Company;
import com.luckia.biller.core.model.Liquidation;
import com.luckia.biller.core.reporting.LiquidationReportGenerator;
import com.luckia.biller.core.services.FileService;
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
	private final Provider<EntityManager> entityManagerProvider;
	private final LiquidationProcessor liquidationProcessor;
	private final LiquidationReportGenerator reportGenerator;
	private final FileService fileService;
	private Liquidation liquidationResult;

	public LiquidationTask(long companyId, Range<Date> range, Injector injector) {
		this.companyId = companyId;
		this.range = range;
		this.liquidationProcessor = injector.getInstance(LiquidationProcessor.class);
		this.entityManagerProvider = injector.getProvider(EntityManager.class);
		this.reportGenerator = injector.getInstance(LiquidationReportGenerator.class);
		this.fileService = injector.getInstance(FileService.class);
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
			LOG.debug("Procesada liquidacion del operador {} de {} en {} ms. Generando report", company.getName(), ISO_DATE_FORMAT.format(range.getMaximum()), ms);
			t0 = System.currentTimeMillis();
			try {
				AppFile appFile = generateReport(liquidationResult, company, entityManager);
				ms = System.currentTimeMillis() - t0;
				LOG.debug("Generado report {} del operador {} en {} ms", appFile.getInternalPath(), company.getName(), ms);
			} catch (Exception ignore) {
				LOG.error("Error al generar el report del operador {}", company.getName(), ignore);
			}

		} catch (Exception ex) {
			LOG.error("Error al procesar la liquidacion", ex);
		}
	}

	private AppFile generateReport(Liquidation liquidation, Company company, EntityManager entityManager) {
		Date from = range.getMinimum();
		Date to = range.getMaximum();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportGenerator.generate(from, to, Arrays.asList(company), out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		String fileName = String.format("Liquidaciones-%s-%s.xls", DateFormatUtils.ISO_DATE_FORMAT.format(from), DateFormatUtils.ISO_DATE_FORMAT.format(to));
		AppFile appFile = fileService.save(fileName, FileService.CONTENT_TYPE_EXCEL, in);
		if (entityManager.getTransaction().isActive()) {
			liquidation.setReportFile(appFile);
			entityManager.merge(liquidation);
		} else {
			entityManager.getTransaction().begin();
			liquidation.setReportFile(appFile);
			entityManager.merge(liquidation);
			entityManager.getTransaction().commit();
		}
		return appFile;
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